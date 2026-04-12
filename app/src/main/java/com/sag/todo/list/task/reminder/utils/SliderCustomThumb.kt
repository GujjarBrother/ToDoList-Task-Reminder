package com.sag.todo.list.task.reminder.utils

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.google.android.material.slider.Slider
import com.sag.todo.list.task.reminder.R
import com.sag.todo.list.task.reminder.utils.AppConstants.getDrawableResource

class SliderCustomThumb @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : Slider(context, attrs) {

    private val customThumb = context.getDrawableResource(R.drawable.slider_thumb)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Calculate thumb position
        val thumbX = trackSidePadding + (value - valueFrom) / (valueTo - valueFrom) * (width - 2 * trackSidePadding)
        val thumbY = height / 2F

        customThumb?.setBounds(
            (thumbX - 35).toInt(), (thumbY - 35).toInt(),
            (thumbX + 35).toInt(), (thumbY + 35).toInt()
        )
        customThumb?.draw(canvas)
    }
}