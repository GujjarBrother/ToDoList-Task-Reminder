package com.sag.todo.list.task.reminder.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sag.todo.list.task.reminder.models.ToDoTask
import com.sag.todo.list.task.reminder.repositories.TasksRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(private val tasksRepo: TasksRepo) : ViewModel() {

    fun saveTask(
        toDoTask: ToDoTask,
        isPastTimeCallback: () -> Unit,
        isAlreadySavedCallback: () -> Unit,
        isSavedSuccessfully: (Boolean, Long) -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        when {
            toDoTask.dateAndTimeInMillis.time < System.currentTimeMillis() -> isPastTimeCallback.invoke()
            tasksRepo.isTaskAlreadySaved(toDoTask) >= 1 -> isAlreadySavedCallback.invoke()
            else -> {
                val savedTaskID = tasksRepo.saveTask(toDoTask)
                isSavedSuccessfully.invoke(savedTaskID >= 1, savedTaskID)
            }
        }
    }

    fun getAllTasks(isTaskComplete: Boolean) = tasksRepo.getAllTasks(isTaskComplete)

    fun updateTask(
        toDoTask: ToDoTask,
        isPastTimeCallback: () -> Unit,
        isAlreadySavedCallback: () -> Unit,
        isUpdatedSuccessfullyCallback: (Boolean) -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        when {
            toDoTask.dateAndTimeInMillis.time < System.currentTimeMillis() -> isPastTimeCallback.invoke()
            tasksRepo.isTaskAlreadySaved(toDoTask) >= 1 -> isAlreadySavedCallback.invoke()
            tasksRepo.updateTask(toDoTask) == 1 -> isUpdatedSuccessfullyCallback.invoke(true)
        }
    }

    fun deleteTask(
        toDoTask: ToDoTask,
        isDeleteCallback: (Boolean) -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        isDeleteCallback.invoke(tasksRepo.deleteTask(toDoTask) == 1)
    }

    fun updateCompletedAndTimeUpTasks(completed: Boolean, taskCompletedTime: Date) = viewModelScope.launch(Dispatchers.IO) {
        tasksRepo.updateCompletedAndTimeUpTasks(completed, taskCompletedTime)
    }
}