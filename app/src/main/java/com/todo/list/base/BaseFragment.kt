package com.todo.list.base

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.todo.list.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

open class BaseFragment : Fragment() {

    protected lateinit var fragmentContext: Activity

    protected var defaultColor = 0
    protected var darkYellowColor = 0
    protected var orangeColor = 0
    protected var lightGreenColor = 0
    protected var blueColor = 0
    protected var cyanColor = 0
    protected var pinkColor = 0
    protected var darkBlueColor = 0
    protected var redColor = 0
    protected var lightPurpleColor = 0
    protected var snowWhiteColor = 0
    protected var screensNightModeColor = 0
    protected var cardsNightModeColor = 0
    protected var whiteColor = 0
    protected var blackColor = 0
    protected var fragmentsCardViewsColor = 0

    private var dialogBoxesLightModeBackground = 0
    protected var dialogBoxesDarkModeBackground = 0

    protected var spinnerLayoutNightModeBackground = 0

    protected lateinit var editTextsCursorDarkModeColor: Drawable
    protected lateinit var listViewStyleImage: Drawable
    protected lateinit var gridViewStyleImage: Drawable

    protected lateinit var whiteColorStateList: ColorStateList
    protected lateinit var textInputLayoutBoxStrokeDarkModeColor: ColorStateList

    protected val simpleDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    protected val monthSimpleDateFormat = SimpleDateFormat("MMM", Locale.getDefault())
    protected val dateOfMonthSimpleDateFormat = SimpleDateFormat("dd", Locale.getDefault())
    protected val yearSimpleDateFormat = SimpleDateFormat("yyyy", Locale.getDefault())
    protected val simpleTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    protected var calendar: Calendar = Calendar.getInstance()

    protected lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fragmentContext = requireActivity()

        handler = Handler(Looper.getMainLooper())

        defaultColor = ContextCompat.getColor(fragmentContext, R.color.defaultColor)
        darkYellowColor = ContextCompat.getColor(fragmentContext, R.color.darkYellowColor)
        orangeColor = ContextCompat.getColor(fragmentContext, R.color.orangeColor)
        lightGreenColor = ContextCompat.getColor(fragmentContext, R.color.lightGreenColor)
        blueColor = ContextCompat.getColor(fragmentContext, R.color.blueColor)
        cyanColor = ContextCompat.getColor(fragmentContext, R.color.cyanColor)
        pinkColor = ContextCompat.getColor(fragmentContext, R.color.pinkColor)
        darkBlueColor = ContextCompat.getColor(fragmentContext, R.color.darkBlueColor)
        redColor = ContextCompat.getColor(fragmentContext, R.color.redColor)
        lightPurpleColor = ContextCompat.getColor(fragmentContext, R.color.lightPurpleColor)
        screensNightModeColor = ContextCompat.getColor(fragmentContext, R.color.screensNightModeColor)
        cardsNightModeColor = ContextCompat.getColor(fragmentContext, R.color.cardsNightModeColor)
        whiteColor = ContextCompat.getColor(fragmentContext, R.color.whiteColor)
        blackColor = ContextCompat.getColor(fragmentContext, R.color.blackColor)
        fragmentsCardViewsColor = ContextCompat.getColor(fragmentContext, R.color.fragmentsCardViewsColor)

        dialogBoxesLightModeBackground = R.drawable.dialog_boxes_light_mode_background
        dialogBoxesDarkModeBackground = R.drawable.dialog_boxes_dark_mode_background
        spinnerLayoutNightModeBackground = R.drawable.spinner_layout_dark_mode_background

        editTextsCursorDarkModeColor = ContextCompat.getDrawable(fragmentContext, R.drawable.edittexts_cursor_dark_mode_color) as Drawable
        listViewStyleImage = ContextCompat.getDrawable(fragmentContext, R.drawable.list_view_style_image) as Drawable
        gridViewStyleImage = ContextCompat.getDrawable(fragmentContext, R.drawable.grid_view_style_image) as Drawable

        whiteColorStateList = ColorStateList.valueOf(whiteColor)
        textInputLayoutBoxStrokeDarkModeColor = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf(-android.R.attr.state_focused)), intArrayOf( // Color when focused
                whiteColor,  // Color when not focused
                whiteColor))
    }
}
