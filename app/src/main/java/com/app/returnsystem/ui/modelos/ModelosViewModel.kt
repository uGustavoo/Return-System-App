package com.app.returnsystem.ui.modelos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ModelosViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Este é o Fragmento Produtos"
    }
    val text: LiveData<String> = _text
}