package com.sag.todo.list.task.reminder.repositories

import com.sag.todo.list.task.reminder.db.DAO
import com.sag.todo.list.task.reminder.db.ToDoTask
import java.util.Date
import javax.inject.Inject

class TasksRepo @Inject constructor(private val dao: DAO) {

    suspend fun saveTask(toDoTask: ToDoTask) = dao.saveTask(toDoTask)

    fun getAllTasks(isTaskComplete: Boolean) = dao.getAllTasks(isTaskComplete)

    suspend fun isTaskAlreadySaved(
        toDoTask: ToDoTask
    ): Int {
        return dao.isTaskAlreadySaved(
            toDoTask.day,
            toDoTask.date,
            toDoTask.month,
            toDoTask.year,
            toDoTask.title,
            toDoTask.description,
            toDoTask.time,
            toDoTask.category
        )
    }

    suspend fun updateTask(toDoTask: ToDoTask) = dao.updateTask(toDoTask)

    suspend fun deleteTask(toDoTask: ToDoTask) = dao.deleteTask(toDoTask)

    suspend fun updateCompletedAndTimeUpTasks(completed: Boolean, taskCompletedTime: Date) =
        dao.updateCompletedAndTimeUpTasks(completed, taskCompletedTime)
}