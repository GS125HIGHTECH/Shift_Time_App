package com.example.kalkulatorprzesunieciadaty.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.kalkulatorprzesunieciadaty.R
import com.example.kalkulatorprzesunieciadaty.databinding.ActivityMainBinding
import com.example.kalkulatorprzesunieciadaty.viewModel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var selectedDateTime1: Calendar
    private lateinit var selectedDateTime2: Calendar
    private var offsetValue: Int = 0
    private var toast: Toast? = null
    private val datePattern = "^(\\d{4})-(0?[1-9]|1[0-2])-(0?[1-9]|[12][0-9]) (\\d{2}):(\\d{2})$"
    private val pattern: Pattern = Pattern.compile(datePattern)


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        val view = binding.root
        setContentView(view)
        binding.spinner.setSelection(0)
        val checkBox = binding.checkBox
        val dateEdit = binding.editText2
        val unitEdit = binding.editText
        dateEdit.visibility = View.INVISIBLE
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            binding.button.isEnabled = !isChecked
            val today = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val formattedDate = sdf.format(today.time)
            binding.editText2.setText(formattedDate)
            if (isChecked) {
                dateEdit.visibility = View.VISIBLE
                dateEdit.isEnabled = true
            } else {
                dateEdit.visibility = View.INVISIBLE
                dateEdit.isEnabled = false
            }
        }

        dateEdit.setOnEditorActionListener { _, actionId, _ ->
            unitEdit.clearFocus()
            if (actionId == EditorInfo.IME_ACTION_DONE &&
                handleEditText() &&
                checkBox.isChecked &&
                dateEdit.text.isNotEmpty()) {
                dateEdit.clearFocus()

                unitEdit.clearFocus()
                val enteredDate = dateEdit.text.toString().trim()
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                try {
                    val parsedDate = sdf.parse(enteredDate)
                    if (parsedDate != null && isValidDate(dateEdit.text.toString())) {
                        selectedDateTime1 = Calendar.getInstance()
                        selectedDateTime1.time = parsedDate
                        val offsetUnit = getOffsetUnit()
                        selectedDateTime2 = Calendar.getInstance()
                        selectedDateTime2.timeInMillis = selectedDateTime1.timeInMillis
                        selectedDateTime2.add(offsetUnit, offsetValue)

                        updateDateTime(selectedDateTime1, selectedDateTime2)
                    } else {
                        toast?.cancel()
                        toast = Toast.makeText(this, R.string.Invalid_Date_Format, Toast.LENGTH_SHORT)
                        toast?.show()
                    }
                } catch (e: Exception) {
                    toast?.cancel()
                    toast = Toast.makeText(this, R.string.Invalid_Date, Toast.LENGTH_SHORT)
                    toast?.show()
                }
            } else if (!handleEditText()){
                toast?.cancel()
                toast = Toast.makeText(this, R.string.Warning, Toast.LENGTH_SHORT)
                toast?.show()
            } else if(!handleEditText2()) {
                    toast?.cancel()
                    toast = Toast.makeText(this, R.string.Invalid_Date, Toast.LENGTH_SHORT)
                    toast?.show()
            }
            false
        }

        unitEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                unitEdit.clearFocus()
                if (!checkBox.isChecked) {
                    binding.button.performClick()
                } else if (handleEditText()) {
                    val enteredValue = unitEdit.text.toString().trim()

                    if (isValidNumber(enteredValue)) {

                        val enteredDate = dateEdit.text.toString().trim()
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

                        try {
                            val parsedDate = sdf.parse(enteredDate)
                            if (parsedDate != null) {
                                selectedDateTime1 = Calendar.getInstance()
                                selectedDateTime1.time = parsedDate
                                val offsetUnit = getOffsetUnit()
                                selectedDateTime2 = Calendar.getInstance()
                                selectedDateTime2.timeInMillis = selectedDateTime1.timeInMillis
                                selectedDateTime2.add(offsetUnit, offsetValue)

                                updateDateTime(selectedDateTime1, selectedDateTime2)
                                unitEdit.clearFocus()
                            } else {
                                toast?.cancel()
                                toast = Toast.makeText(this, R.string.Invalid_Date_Format, Toast.LENGTH_SHORT)
                                toast?.show()
                            }
                        } catch (e: Exception) {
                            toast?.cancel()
                            toast = Toast.makeText(this, R.string.Invalid_Date, Toast.LENGTH_SHORT)
                            toast?.show()
                        }
                    } else {
                        toast?.cancel()
                        toast = Toast.makeText(this, R.string.Warning_Invalid, Toast.LENGTH_SHORT)
                        toast?.show()
                    }
                    unitEdit.clearFocus()
                } else if (!handleEditText2()){
                    toast?.cancel()
                    toast = Toast.makeText(this, R.string.Invalid_Date, Toast.LENGTH_SHORT)
                    toast?.show()
                }
            }
            false
        }

        binding.button.setOnClickListener {
            if (handleEditText()) {
                showDateTimePicker()
            } else {
                toast?.cancel()
                toast = Toast.makeText(this, R.string.Warning, Toast.LENGTH_SHORT)
                toast?.show()
            }
        }

        viewModel.selectedDateTime1.observe(this) { dateTime1 ->
            binding.textView.text = dateTime1.toString()
        }

        viewModel.selectedDateTime2.observe(this) { dateTime2 ->
            binding.textView2.text = dateTime2.toString()
        }

    }

    private fun showDateTimePicker() {
        val currentDateTime = Calendar.getInstance()
        val year = currentDateTime.get(Calendar.YEAR)
        val month = currentDateTime.get(Calendar.MONTH)
        val day = currentDateTime.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val hour = currentDateTime.get(Calendar.HOUR_OF_DAY)
                val minute = currentDateTime.get(Calendar.MINUTE)

                val timePickerDialog = TimePickerDialog(
                    this,
                    { _, selectedHour, selectedMinute ->
                        selectedDateTime1 = Calendar.getInstance()
                        selectedDateTime1.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute)

                        selectedDateTime2 = Calendar.getInstance()
                        selectedDateTime2.timeInMillis = selectedDateTime1.timeInMillis

                        val offsetUnit = getOffsetUnit()
                        selectedDateTime2.add(offsetUnit, offsetValue)
                        updateDateTime(selectedDateTime1, selectedDateTime2)
                        binding.editText.clearFocus()
                    },
                    hour,
                    minute,
                    true
                )
                timePickerDialog.show()
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun updateDateTime(dateTime1: Calendar, dateTime2: Calendar) {
        val formattedDateTime1 = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(dateTime1.time)
        val text = getString(R.string.Selected) + formattedDateTime1

        val formattedDateTime2 = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(dateTime2.time)
        val text2 = getString(R.string.Shifted) + formattedDateTime2
        viewModel.updateSelectedDateTime(text, text2)
    }

    private fun getOffsetUnit(): Int {
        return when (binding.spinner.selectedItemPosition) {
            0 -> Calendar.HOUR_OF_DAY
            1 -> Calendar.DAY_OF_YEAR
            2 -> Calendar.WEEK_OF_YEAR
            3 -> Calendar.MONTH
            4 -> Calendar.YEAR
            else -> Calendar.HOUR_OF_DAY
        }
    }

    private fun handleEditText(): Boolean {
        val shiftValue = binding.editText.text.toString().trim()
        return if (shiftValue.isNotEmpty()) {
            offsetValue = shiftValue.toIntOrNull() ?: 0

            true
        } else {
            false
        }
    }

    private fun handleEditText2(): Boolean {
        val dateValue = binding.editText2.text.toString().trim()
        return dateValue.isNotEmpty()
    }

    private fun isValidNumber(value: String): Boolean {
        val numberRegex = "^[0-9]+$".toRegex()
        return numberRegex.matches(value)
    }


    fun isValidDate(date: String): Boolean {
        val matcher: Matcher = pattern.matcher(date)
        return if (matcher.matches()) {
            matcher.reset()
            if (matcher.find()) {
                val yearStr = matcher.group(1)!! // Year is guaranteed by the pattern
                val year = yearStr.toInt()
                val month = matcher.group(2)!!.toInt() // Month is guaranteed by the pattern
                val day = matcher.group(3)!!.toInt() // Day is guaranteed by the pattern
                val hour = matcher.group(4)!!.toInt() // Hour is guaranteed by the pattern
                val minute = matcher.group(5)!!.toInt() // Minute is guaranteed by the pattern

                // Validate day based on month and leap year (no need for separate checks)
                if (day !in 1..getDaysInMonth(month, year)) {
                    return false
                }

                // Validate hour and minute
                if (hour !in 0..23 || minute !in 0..59) {
                    return false
                }

                true // Valid date format, day within range, and valid hour/minute
            } else {
                false // No match found by the pattern
            }
        } else {
            false // String doesn't match the expected format
        }
    }

    private fun getDaysInMonth(month: Int, year: Int): Int {
        return when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
            else -> throw IllegalArgumentException("Invalid month value: $month")
        }
    }




}