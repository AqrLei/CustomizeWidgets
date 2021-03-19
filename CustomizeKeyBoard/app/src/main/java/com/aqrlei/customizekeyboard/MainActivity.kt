package com.aqrlei.customizekeyboard

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.MotionEvent
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

        et.setOnFocusChangeListener { v, hasFocus ->
            Log.d("HASFocus","$hasFocus")
        }

        window.decorView.setOnTouchListener { v, event ->

            if (event.action == MotionEvent.ACTION_DOWN) {
                Log.d("TouchDown","etStart(${et.x}), etTop(${et.y}), etEnd(${et.x+et.width}), etBottom(${et.y+et.height}) ")
                Log.d("TouchDown","keyboardViewStart(${keyboardView.x}), keyboardViewTop(${keyboardView.y}), keyboardViewEnd(${keyboardView.x+keyboardView.width}), etBottom(${keyboardView.y+keyboardView.height}) ")
                Log.d("TouchDown","downX=${event.x}, downY=${event.y}")

                val isInEt = (event.x in et.x..(et.x+et.width)) &&(event.y in et.y .. (et.y+et.height))
                val isInKeyboard = (event.x in keyboardView.x..(keyboardView.x+keyboardView.width)) &&(event.y in keyboardView.y .. (keyboardView.y+keyboardView.height))
                Log.d("TouchDown","isInEt = ${isInEt}, isInKeyboard=${isInKeyboard}")
            }
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

                override fun onClose(v: View): Boolean {

                    return true
                }
            }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)

    }

    /**
     * 获取设置的最大输入字符数
     */
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


    /**
     * 阻止软键盘弹出，光标正常显示
     */
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