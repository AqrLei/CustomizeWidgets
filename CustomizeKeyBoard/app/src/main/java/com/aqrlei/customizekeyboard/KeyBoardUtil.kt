package com.aqrlei.customizekeyboard

import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.text.InputType
import android.view.View
import android.widget.EditText

/**
 * created by AqrLei on 3/18/21
 */
class KeyBoardUtil(
    private val keyboardView: KeyboardView,
    private val editText: EditText) {
    private val keyboard = Keyboard(editText.context, R.xml.number_simple_keyboard)
    private val listener = object :KeyboardView.OnKeyboardActionListener {

        override fun onPress(primaryCode: Int) {}

        override fun onRelease(primaryCode: Int) {}

        override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
            val start = editText.selectionStart
            when(primaryCode) {
                Keyboard.KEYCODE_DELETE -> {}
                Keyboard.KEYCODE_DONE -> {}

                else -> editText.text?.insert(start, primaryCode.toChar().toString())
            }
        }

        override fun onText(text: CharSequence?) {}

        override fun swipeLeft() {}

        override fun swipeRight() {}

        override fun swipeDown() {}

        override fun swipeUp() {}
    }

    init {
        editText.inputType = InputType.TYPE_NULL
        keyboardView.setOnKeyboardActionListener(listener)
        keyboardView.keyboard = keyboard
        keyboardView.isEnabled = true
        keyboardView.isPreviewEnabled = false
    }

    fun showKeyboard() {
        keyboardView.visibility = View.VISIBLE
    }

    fun hideKeyboard() {
        keyboardView.visibility = View.GONE
    }
}