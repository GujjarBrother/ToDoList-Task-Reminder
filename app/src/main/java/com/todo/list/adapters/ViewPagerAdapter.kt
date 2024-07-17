package com.todo.list.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.todo.list.enums.TabsEnum
import com.todo.list.fragments.AllTasksFragment
import com.todo.list.fragments.CompletedTasksFragment

class ViewPagerAdapter(val activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            TabsEnum.TASKS_TAB.ordinal -> AllTasksFragment()
            else -> CompletedTasksFragment()
        }
    }
}