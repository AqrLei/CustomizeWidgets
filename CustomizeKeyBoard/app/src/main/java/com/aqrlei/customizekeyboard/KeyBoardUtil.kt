package com.aqrlei.customizekeyboard

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.text.InputFilter
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

/**
 * created by AqrLei on 3/18/21
 */

@SuppressLint("ClickableViewAccessibility")
class KeyBoardUtil( ){

    private var et: EditText? = null
    private var keyboardView: NumberSimpleKeyboardView? = null

    fun bind(context: AppCompatActivity, keyboardView: NumberSimpleKeyboardView, et: EditText) {
        this.et = et
        this.keyboardView = keyboardView

        hackEditText(et)

        et.setOnTouchListener { _, ev ->
            keyboardView.visibility = View.VISIBLE
            Log.d(TAG, "${ev.action}")
            false
        }

        val keyboardCoverView = FrameLayout(context).also {
            it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            it.setBackgroundColor(Color.TRANSPARENT)
        }

        keyboardCoverView.setOnTouchListener { _, event ->

            if (event.action == MotionEvent.ACTION_DOWN) {
                Log.d(
                    "TouchDown",
                    "etStart(${et.x}), etTop(${et.y}), etEnd(${et.x + et.width}), etBottom(${et.y + et.height}) "
                )
                Log.d(
                    "TouchDown",
                    "keyboardViewStart(${keyboardView.x}), keyboardViewTop(${keyboardView.y}), keyboardViewEnd(${keyboardView.x + keyboardView.width}), etBottom(${keyboardView.y + keyboardView.height}) "
                )
                Log.d("TouchDown", "downX=${event.x}, downY=${event.y}")

                val etRangeX = et.x..(et.x + et.width)
                val etRangeY = et.y..(et.y + et.height)
                val keyboardRangeX = keyboardView.x..(keyboardView.x + keyboardView.width)
                val keyboardRangeY = keyboardView.y..(keyboardView.y + keyboardView.height)

                Log.d("TouchDown", "etRangeX = ${etRangeX}, etRangeY=${etRangeY}")
                Log.d("TouchDown", "keyboardRangeX= ${keyboardRangeX}, keyboardRangeY=${keyboardRangeY}")

                val isInEt = (event.x in etRangeX) && (event.y in etRangeY)
                val isInKeyboard = (event.x in keyboardRangeX) && (event.y in keyboardRangeY)
                Log.d("TouchDown", "isInEt = ${isInEt}, isInKeyboard=${isInKeyboard}")

                if (!isInEt && !isInKeyboard) {
                    keyboardView.visibility = View.GONE
                }
            }

            Log.d("TouchDown", "aciton: ${event.action}")

            false
        }

        context.window.decorView.findViewById<ViewGroup>(android.R.id.content)
            ?.addView(keyboardCoverView)

        keyboardView.listener =
            object : OnKeyInputListener {
                override fun onDelete() {
                    val deleteIndex = et.selectionStart - 1 // 移除光标前一位的字符
                    val contentSize = et.text.toString().length

                    if (deleteIndex < 0 || deleteIndex > contentSize) return

                    et.text.toString().takeIf { it.isNotEmpty() }?.let {
                        et.setText(it.removeRange(deleteIndex, deleteIndex + 1))
                        et.setSelection(deleteIndex, deleteIndex)
                    }
                }

                override fun onInput(text: String?) {
                    var realInputText = text ?: ""
                    val lengthFilter =
                        (et.filters.find { it is InputFilter.LengthFilter } as? InputFilter.LengthFilter)
                    val content = et.text.toString()
                    hackLengthFilterMax(lengthFilter)?.let { maxLength ->
                        val insertLength = maxLength - content.length // 有最大输入数限制，获取还可以数入多少字符
                        if (insertLength > 0 && insertLength < realInputText.length) {
                            realInputText = realInputText.substring(
                                0,
                                insertLength.coerceAtMost(realInputText.length)
                            )
                        } else if (insertLength <= 0) { // 超过限制， 则这次输入无效
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

    fun showKeyboard() {
        if (et?.hasFocus() != true) {
            et?.requestFocus()
        }
        keyboardView?.visibility = View.VISIBLE
    }

    fun hideKeyboard() {
        keyboardView?.visibility = View.GONE
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
    private fun hackEditText(edit: EditText?) {
        edit ?: return
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