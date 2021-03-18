package com.aqrlei.customizekeyboard

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val et = findViewById<EditText>(R.id.etInput)
        val keyboardView = findViewById<NumberSimpleKeyboardView>(R.id.numberKeyboard)
        et.inputType = InputType.TYPE_NULL

        et.setOnTouchListener { _, _ ->
            keyboardView.visibility = View.VISIBLE
            false
        }

        keyboardView.listener =
            object : NumberSimpleKeyboardView.OnKeyListener {
                override fun onDelete() {
                    et.text.toString().takeIf { it.isNotEmpty() }?.let { content ->
                        et.setText(content.substring(0, content.length - 1))
                    }
                }

                override fun onInput(text: String?) {
                    et.append(text)
                }

                override fun onClose(v: View) {

                }
            }
    }
}