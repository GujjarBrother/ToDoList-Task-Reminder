package com.sag.todo.list.task.reminder.models

data class TasksUiState(
    val tasks: List<ToDoTask> = emptyList(),
    val isLoading: Boolean = false
)