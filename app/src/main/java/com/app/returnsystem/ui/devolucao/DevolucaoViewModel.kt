package com.app.returnsystem.ui.devolucao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DevolucaoViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Este é o Fragmento Devolução"
    }
    val text: LiveData<String> = _text
}