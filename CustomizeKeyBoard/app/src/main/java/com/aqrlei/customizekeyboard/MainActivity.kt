package com.aqrlei.customizekeyboard

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val et = findViewById<EditText>(R.id.etInput)
        val keyboardView = findViewById<NumberSimpleKeyboardView>(R.id.numberKeyboard)
        val keyboardUtil = KeyBoardUtil()
        keyboardUtil.bind(this, keyboardView, et)

        findViewById<View>(R.id.tvHelloWorld).setOnClickListener {
            Log.d("TouchDown", "hello world click")
            Toast.makeText(this,"hello world",Toast.LENGTH_SHORT).show()
        }
    }

}