package com.example.kalkulatorprzesunieciadaty.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _selectedDateTime1 = MutableLiveData<String>()
    val selectedDateTime1: LiveData<String> = _selectedDateTime1

    private val _selectedDateTime2 = MutableLiveData<String>()
    val selectedDateTime2: LiveData<String> = _selectedDateTime2

    fun updateSelectedDateTime(dateTime1: String, dateTime2: String) {
        _selectedDateTime1.value = dateTime1
        _selectedDateTime2.value = dateTime2
    }
}