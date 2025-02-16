package com.todo.list.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.todo.list.R

object ColorsUtils {
    val defaultColor = R.color.defaultColor
    val defaultTransparentColor = R.color.defaultTransparentColor
    val whiteColor = R.color.whiteColor
    val blackColor = R.color.blackColor
    val darkModeTextColor = R.color.purple_500
    val tabLayoutUnSelectedTabTextColor = R.color.tabLayoutUnSelectedTabTextColor
    val fragmentsCardViewsColor = R.color.fragmentsCardViewsColor

    fun getContextCompatColor(context: Context, color: Int) = ContextCompat.getColor(context, color)

    fun getContextCompatDrawable(context: Context, drawableResource: Int) =
        ContextCompat.getDrawable(context, drawableResource) as Drawable
}