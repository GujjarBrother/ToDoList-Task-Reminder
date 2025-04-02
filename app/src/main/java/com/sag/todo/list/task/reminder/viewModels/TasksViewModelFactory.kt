package com.sag.todo.list.task.reminder.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sag.todo.list.task.reminder.repositories.TasksRepo

class TasksViewModelFactory(private val tasksRepo: TasksRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TasksViewModel(tasksRepo) as T
    }
}