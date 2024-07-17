package com.todo.list.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.todo.list.repositories.TasksRepo

class TasksViewModelFactory(private val tasksRepo: TasksRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TasksViewModel(tasksRepo) as T
    }
}