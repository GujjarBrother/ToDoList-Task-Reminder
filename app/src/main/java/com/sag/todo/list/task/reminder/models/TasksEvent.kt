package com.sag.todo.list.task.reminder.models

sealed class TasksEvent {
    object ShowPastTimeError : TasksEvent()
    object ShowAlreadyExistsError : TasksEvent()
    class TaskSaved(isSavedSuccessfully: Boolean, savedTaskID: Long) : TasksEvent()
    object TaskUpdated : TasksEvent()
    object TaskDeleted : TasksEvent()
}