package com.app.returnsystem.ui.coleta

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ColetaViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Esse é o Fragmento Coleta"
    }
    val text: LiveData<String> = _text
}