package com.sag.todo.list.task.reminder.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sag.todo.list.task.reminder.db.ToDoTask
import com.sag.todo.list.task.reminder.repositories.TasksRepo
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.Date

class TasksViewModel(private val tasksRepo: TasksRepo) : ViewModel() {

    fun saveTask(toDoTask: ToDoTask): Deferred<Long> = viewModelScope.async(Dispatchers.IO) {
        tasksRepo.saveTask(toDoTask)
    }

    fun getAllTasks(isTaskComplete: Boolean) = tasksRepo.getAllTasks(isTaskComplete)

    fun isTaskAlreadySaved(
        dayOfWeek: String,
        date: String,
        month: String,
        year: String,
        title: String,
        description: String,
        time: String,
        category: Int
    ): Deferred<Int> = viewModelScope.async(Dispatchers.IO) {
        tasksRepo.isTaskAlreadySaved(dayOfWeek, date, month, year, title, description, time, category)
    }

    fun updateTask(toDoTask: ToDoTask): Deferred<Int> = viewModelScope.async(Dispatchers.IO) {
        tasksRepo.updateTask(toDoTask)
    }

    fun deleteTask(toDoTask: ToDoTask): Deferred<Int> = viewModelScope.async(Dispatchers.IO) {
        tasksRepo.deleteTask(toDoTask)
    }

    fun updateCompletedAndTimeUpTasks(completed: Boolean, taskCompletedTime: Date) = viewModelScope.launch(Dispatchers.IO) {
        tasksRepo.updateCompletedAndTimeUpTasks(completed, taskCompletedTime)
    }
}