package com.app.returnsystem.ui.sobre

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.returnsystem.databinding.ActivitySobreBinding

class SobreActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySobreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySobreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar!!.hide()
    }
}