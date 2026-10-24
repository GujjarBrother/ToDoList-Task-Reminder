package com.sag.todo.task.reminder.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sag.todo.task.reminder.enums.Tabs
import com.sag.todo.task.reminder.fragments.AllTasksFragment

class ViewPagerAdapter(val activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount() = Tabs.entries.size

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            Tabs.TASKS_TAB.ordinal -> AllTasksFragment.newInstance(Tabs.TASKS_TAB.ordinal)
            else -> AllTasksFragment.newInstance(Tabs.COMPLETED_TAB.ordinal)
        }
    }
}