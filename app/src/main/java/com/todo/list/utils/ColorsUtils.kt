package com.todo.list.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.todo.list.R
import com.todo.list.models.SelectedColors

object ColorsUtils {
    val defaultColor = R.color.defaultColor
    val darkYellowColor = R.color.darkYellowColor
    val orangeColor = R.color.orangeColor
    val lightGreenColor = R.color.lightGreenColor
    val blueColor = R.color.blueColor
    val cyanColor = R.color.cyanColor
    val pinkColor = R.color.pinkColor
    val darkBlueColor = R.color.darkBlueColor
    val redColor = R.color.redColor
    val lightPurpleColor = R.color.lightPurpleColor

    val defaultTransparentColor = R.color.defaultTransparentColor
    val darkYellowTransparentColor = R.color.darkYellowTransparentColor
    val orangeTransparentColor = R.color.orangeTransparentColor
    val lightGreenTransparentColor = R.color.lightGreenTransparentColor
    val blueTransparentColor = R.color.blueTransparentColor
    val cyanTransparentColor = R.color.cyanTransparentColor
    val pinkTransparentColor = R.color.pinkTransparentColor
    val darkBlueTransparentColor = R.color.darkBlueTransparentColor
    val redTransparentColor = R.color.redTransparentColor
    val lightPurpleTransparentColor = R.color.lightPurpleTransparentColor

    val snowWhiteColor = R.color.snowWhiteColor
    val switchTrackOffColor = R.color.switchTrackOffColor
    val subTitlesTextColor = R.color.subTitlesTextColor
    val screensNightModeColor = R.color.screensNightModeColor
    val cardsNightModeColor = R.color.cardsNightModeColor
    val whiteColor = R.color.whiteColor
    val blackColor = R.color.blackColor
    val lightBlackColor = R.color.lightBlackColor
    val recyclerViewsDividerColor = R.color.recyclerViewsDividerColor
    val lightBlueColor = R.color.lightBlueColor
    val darkModeTextColor = R.color.purple_500
    val feedbackEditTextCardViewLightModeColor = R.color.feedbackEditTextCardViewLightModeColor
    val tabLayoutUnSelectedTabTextColor = R.color.tabLayoutUnSelectedTabTextColor
    val fragmentsCardViewsColor = R.color.fragmentsCardViewsColor

    fun getSelectedColor(context: Context, prefs: Prefs) = when(prefs.colorSchemeValue) {
        0 -> {
            SelectedColors(
                ContextCompat.getColor(context, defaultColor), ContextCompat.getColor(context, defaultTransparentColor)
            )
        }

        1 -> {
            SelectedColors(
                ContextCompat.getColor(context, darkYellowColor), ContextCompat.getColor(context, darkYellowTransparentColor)
            )
        }

        2 -> {
            SelectedColors(
                ContextCompat.getColor(context, orangeColor), ContextCompat.getColor(context, orangeTransparentColor)
            )
        }

        3 -> {
            SelectedColors(
                ContextCompat.getColor(context, lightGreenColor), ContextCompat.getColor(context, lightGreenTransparentColor)
            )
        }

        4 -> {
            SelectedColors(
                ContextCompat.getColor(context, blueColor), ContextCompat.getColor(context, blueTransparentColor)
            )
        }

        5 -> {
            SelectedColors(
                ContextCompat.getColor(context, cyanColor), ContextCompat.getColor(context, cyanTransparentColor)
            )
        }

        6 -> {
            SelectedColors(
                ContextCompat.getColor(context, pinkColor), ContextCompat.getColor(context, pinkTransparentColor)
            )
        }

        7 -> {
            SelectedColors(
                ContextCompat.getColor(context, darkBlueColor), ContextCompat.getColor(context, darkBlueTransparentColor)
            )
        }

        8 -> {
            SelectedColors(
                ContextCompat.getColor(context, redColor), ContextCompat.getColor(context, redTransparentColor)
            )
        }

        else -> {
            SelectedColors(
                ContextCompat.getColor(context, lightPurpleColor), ContextCompat.getColor(context, lightPurpleTransparentColor)
            )
        }
    }

    fun getContextCompatColor(context: Context, color: Int) = ContextCompat.getColor(context, color)

    fun getContextCompatDrawable(context: Context, drawableResource: Int) =
        ContextCompat.getDrawable(context, drawableResource) as Drawable
}