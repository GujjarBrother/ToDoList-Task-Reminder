package com.todo.list.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.todo.list.fragments.AllTasksFragment
import com.todo.list.fragments.CompletedTasksFragment
import com.todo.list.utils.CommonFunctions.TASKS_TAB

class ViewPagerAdapter(val activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            TASKS_TAB -> {
                AllTasksFragment()
            }

            else -> {
                CompletedTasksFragment()
            }
        }
    }
}
