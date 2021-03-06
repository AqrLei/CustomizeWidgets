package com.aqrlei.customizekeyboard

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View


/**
 * created by AqrLei on 3/18/21
 */
const val KEY_EMPTY = -10
const val KEY_WRAP = -11
const val KEY_DIVIDER =  -12

interface OnKeyInputListener {
    fun onDelete()
    fun onInput(text: String?)
    fun onClose(v: View) : Boolean
}

val TAG = NumberSimpleKeyboardView::class.java.simpleName

class NumberSimpleKeyboardView(context: Context, attrs: AttributeSet?) :
    KeyboardView(context, attrs), KeyboardView.OnKeyboardActionListener {

    var listener: OnKeyInputListener? = null

    private val delKeyBackgroundColor = (0xffDADADA).toInt()

    private var keyIconRect: Rect? = null
    private val paint = Paint()

    init {
        val keyboard = Keyboard(context, R.xml.number_simple_keyboard)
        setKeyboard(keyboard)
        isEnabled = true
        isFocusable = true
        isPreviewEnabled = false
        onKeyboardActionListener = this

        paint.textAlign = Paint.Align.CENTER
        val font = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.typeface = font
        paint.isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        keyboard?.keys?.takeIf { it.size > 0 }?.let {
            for (key in it) {
                when (key?.codes?.get(0)) {
                    KEY_EMPTY -> {
                        drawKeyBackground(key, canvas, delKeyBackgroundColor)
                    }
                    KEY_WRAP -> {
                        drawKeyBackground(key, canvas, Color.WHITE)
                        drawKeyIcon(key,canvas, resources.getDrawable(R.drawable.ic_down))
                    }

                    KEY_DIVIDER -> {
                       drawKeyBackground(key, canvas, Color.parseColor("#ffDADADA"))
                    }

                    Keyboard.KEYCODE_DELETE -> {
                        drawKeyBackground(key, canvas, delKeyBackgroundColor)
                        drawKeyIcon(key, canvas, resources.getDrawable(R.drawable.ic_delete))
                    }

                    else -> {
                        drawKeyBackground(key, canvas, Color.WHITE)
                        drawKeyLabel(key, canvas)
                    }
                }
            }
        }
    }

    private fun drawKeyBackground(key: Keyboard.Key, canvas: Canvas, color: Int) {
        val drawable = ColorDrawable(color)
        drawable.setBounds(key.x, key.y, key.x + key.width, key.y + key.height)
        drawable.draw(canvas)
    }

    private fun drawKeyLabel(key: Keyboard.Key, canvas: Canvas) {
        paint.textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            28F,
            resources.displayMetrics
        )
        paint.color = Color.parseColor("#FF666666")

        val rect = Rect(key.x, key.y, key.x + key.width, key.y + key.height)
        val fontMetrics: Paint.FontMetricsInt = paint.fontMetricsInt
        val baseline: Int = (rect.bottom + rect.top - fontMetrics.bottom - fontMetrics.top) / 2
        canvas.drawText(
            key.label.toString(),
            rect.centerX().toFloat(), baseline.toFloat(), paint
        )
    }

    private fun drawKeyIcon(key: Keyboard.Key, canvas: Canvas, iconDrawable: Drawable?) {
        iconDrawable ?: return
//        if (keyIconRect == null || keyIconRect!!.isEmpty) {
            val intrinsicWidth = iconDrawable.intrinsicWidth
            val intrinsicHeight = iconDrawable.intrinsicHeight
            var drawWidth = intrinsicWidth
            var drawHeight = intrinsicHeight
            if (drawWidth > key.width) {
                drawWidth = key.width
                drawHeight = (drawWidth * 1.0f / intrinsicWidth * intrinsicHeight).toInt()
            } else if (drawHeight > key.height) {
                drawHeight = key.height
                drawWidth = (drawHeight * 1.0f / intrinsicHeight * intrinsicWidth).toInt()
            }
            val left = key.x + key.width / 2 - drawWidth / 2
            val top = key.y + key.height / 2 - drawHeight / 2
            keyIconRect = Rect(left, top, left + drawWidth, top + drawHeight)
//        }
        if (keyIconRect != null && !keyIconRect!!.isEmpty) {
            iconDrawable.bounds = keyIconRect!!
            iconDrawable.draw(canvas)
        }
    }

    override fun swipeLeft() {
        Log.d(TAG, "swipeLeft")
    }

    override fun swipeRight() {
        Log.d(TAG, "swipeRight")
    }

    override fun swipeDown() {
        Log.d(TAG, "swipeDown")
    }

    override fun swipeUp() {
        Log.d(TAG, "swipeUp")
    }

    override fun onPress(primaryCode: Int) {
        Log.d(TAG, "onPress: ${primaryCode.toChar()}")
    }

    override fun onRelease(primaryCode: Int) {
        Log.d(TAG, "onRelease: ${primaryCode.toChar()}")
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        Log.d(TAG, "onKey: ${primaryCode.toChar()}, ${keyCodes?.contentToString()}")
        when (primaryCode) {
            KEY_EMPTY -> return
            KEY_WRAP -> {
                if (listener?.onClose(this) != false) {
                    this.visibility = View.GONE
                }
                listener?.onClose(this)
            }
            Keyboard.KEYCODE_DELETE -> listener?.onDelete()
            else -> listener?.onInput(primaryCode.toChar().toString())
        }
    }

    override fun onText(text: CharSequence?) {
        Log.d(TAG, "onText: ${text}")
    }
}