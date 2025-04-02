package com.sag.todo.list.task.reminder.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sag.todo.list.task.reminder.enums.Tabs
import com.sag.todo.list.task.reminder.fragments.AllTasksFragment

class ViewPagerAdapter(val activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            Tabs.TASKS_TAB.ordinal -> AllTasksFragment.newInstance(Tabs.TASKS_TAB.ordinal)
            else -> AllTasksFragment.newInstance(Tabs.COMPLETED_TAB.ordinal)
        }
    }
}