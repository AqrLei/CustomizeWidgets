package com.aqrlei.customizekeyboard

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
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
        hackEditText(et)
        et.requestFocus()
        et.setOnTouchListener { _, _ ->
            keyboardView.visibility = View.VISIBLE
            false
        }

        keyboardView.listener =
            object : NumberSimpleKeyboardView.OnKeyListener {
                override fun onDelete() {


                    Log.d(
                        "Keyboard",
                        "onDelete - selectionStart:${et.selectionStart}, selectionEnd:${et.selectionEnd}"
                    )
                    val deleteIndex = et.selectionStart - 1
                    val contentSize = et.text.toString().length

                    if (deleteIndex < 0 || deleteIndex > contentSize) return

                    et.text.toString().takeIf { it.isNotEmpty() }?.let {
                        et.setText(it.removeRange(deleteIndex, deleteIndex + 1))
                        et.setSelection(deleteIndex, deleteIndex)
                    }
                }

                override fun onInput(text: String?) {
                    Log.d(
                        "Keyboard",
                        "onInput - selectionStart:${et.selectionStart}, selectionEnd:${et.selectionEnd}"
                    )
                    var realInputText = text ?: ""
                    et.filters
                    val lengthFilter =
                        (et.filters.find { it is InputFilter.LengthFilter } as? InputFilter.LengthFilter)
                    val content = et.text.toString()
                    hackLengthFilterMax(lengthFilter)?.let { maxLength ->
                        val insertLength = maxLength - content.length
                        if (insertLength > 0 && insertLength < realInputText.length) {
                            realInputText = realInputText.substring(
                                0,
                                insertLength.coerceAtMost(realInputText.length))
                        } else if (insertLength <= 0) {
                            return@onInput
                        }
                    }

                    if (content.isEmpty()) {
                        et.setText(realInputText)
                        et.setSelection(realInputText.length)
                    } else {
                        content.let {
                            val insertIndex = et.selectionEnd
                            val prefix = it.substring(0, insertIndex)
                            val suffix = if (insertIndex < it.length) it.substring(
                                insertIndex,
                                it.length
                            ) else ""

                            et.setText("$prefix$realInputText$suffix")
                            val resultSelectionIndex = insertIndex + (realInputText.length)
                            et.setSelection(resultSelectionIndex, resultSelectionIndex)
                        }
                    }
                }

                override fun onClose(v: View) {

                }
            }
    }

    private fun hackLengthFilterMax(lengthFilter: InputFilter.LengthFilter?): Int? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            lengthFilter?.max
        } else {
            val field = InputFilter.LengthFilter::class.java.getDeclaredField("mMax").also {
                it.isAccessible = true
            }
            field.get(lengthFilter) as? Int
        }
    }

    private fun hackEditText(edit: EditText) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            edit.showSoftInputOnFocus = false
        } else {
            try {
                //setShowSoftInputOnFocus
                val cls = EditText::class.java
                val setSoftInputShownOnFocus =
                    cls.getMethod("setShowSoftInputOnFocus", Boolean::class.java)
                setSoftInputShownOnFocus.isAccessible = true
                setSoftInputShownOnFocus.invoke(edit, false)
            } catch (e: Exception) {
                Log.d("Keyboard", "$e")
            }
        }
    }
}