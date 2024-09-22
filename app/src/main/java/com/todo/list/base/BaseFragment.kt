package com.todo.list.base

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.todo.list.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

open class BaseFragment : Fragment() {

    protected lateinit var fragmentContext: FragmentActivity

    protected lateinit var textInputLayoutBoxStrokeDarkModeColor: ColorStateList
    protected val simpleDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    protected val simpleTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    protected val simpleDateAndTimeFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
    protected var calendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fragmentContext = requireActivity()
        textInputLayoutBoxStrokeDarkModeColor = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf(-android.R.attr.state_focused)),
            intArrayOf(
                // Color when focused
                ContextCompat.getColor(fragmentContext, R.color.purple_500),
                // Color when not focused
                ContextCompat.getColor(fragmentContext, R.color.purple_500))
        )
    }
}
