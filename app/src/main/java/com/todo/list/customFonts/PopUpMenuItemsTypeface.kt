package com.todo.list.customFonts

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.TypefaceSpan
import androidx.annotation.ColorInt

class PopUpMenuItemsTypefaceAndColor(
    family: String?,
    private val customFont: Typeface,
    @ColorInt private val customColor: Int
) : TypefaceSpan(family) {
    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.color = customColor
        applyCustomTypeFace(ds, customFont)
    }

    override fun updateMeasureState(paint: TextPaint) {
        super.updateMeasureState(paint)
        applyCustomTypeFace(paint, customFont)
    }

    private fun applyCustomTypeFace(paint: Paint, tf: Typeface) {
        val oldStyle: Int
        val old = paint.typeface
        oldStyle = old?.style ?: 0
        val fake = oldStyle and tf.style.inv()
        if (fake and Typeface.BOLD != 0) {
            paint.isFakeBoldText = true
        }
        if (fake and Typeface.ITALIC != 0) {
            paint.textSkewX = -0.25F
        }
        paint.typeface = tf
    }
}