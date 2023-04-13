package com.app.returnsystem.ui.status

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StatusViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Este é o Fragmento Statis"
    }
    val text: LiveData<String> = _text
}