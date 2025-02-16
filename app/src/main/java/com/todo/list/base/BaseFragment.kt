package com.todo.list.base

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.todo.list.R
import com.todo.list.utils.Prefs
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
open class BaseFragment : Fragment() {

    @Inject
    lateinit var prefs: Prefs

    protected lateinit var fragmentContext: FragmentActivity

    protected val simpleDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    protected val simpleTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    protected val simpleDateAndTimeFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
    protected var calendar: Calendar = Calendar.getInstance()
    protected lateinit var textInputLayoutDarkModeStrokeColor: ColorStateList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fragmentContext = requireActivity()

        textInputLayoutDarkModeStrokeColor = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
            intArrayOf(
                // Color when focused
                ContextCompat.getColor(fragmentContext, R.color.defaultColor),
                // Color when not focused
                ContextCompat.getColor(fragmentContext, R.color.subColor))
        )
    }
}