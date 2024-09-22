package com.todo.list.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.todo.list.enums.Tabs
import com.todo.list.fragments.AllTasksFragment

class ViewPagerAdapter(val activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            Tabs.TASKS_TAB.ordinal -> AllTasksFragment.newInstance(Tabs.TASKS_TAB.ordinal)
            else -> AllTasksFragment.newInstance(Tabs.COMPLETED_TAB.ordinal)
        }
    }
}