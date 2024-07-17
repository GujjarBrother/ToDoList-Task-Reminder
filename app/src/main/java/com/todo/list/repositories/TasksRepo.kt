package com.todo.list.repositories

import com.todo.list.db.DAO
import com.todo.list.db.ToDoTask
import java.util.Date

class TasksRepo(private val dao: DAO) {

    suspend fun saveTask(toDoTask: ToDoTask) = dao.saveTask(toDoTask)

    fun getAllTasks(isCompletedAndTimeUp: Boolean) = dao.getAllTasks(isCompletedAndTimeUp)

    suspend fun isTaskAlreadySaved(
        dayOfWeek: String,
        date: String,
        month: String,
        year: String,
        title: String,
        description: String,
        time: String,
        category: Int
    ): Int {
        return dao.isTaskAlreadySaved(dayOfWeek, date, month, year, title, description, time, category)
    }

    suspend fun updateTask(toDoTask: ToDoTask) = dao.updateTask(toDoTask)

    suspend fun deleteTask(toDoTask: ToDoTask) = dao.deleteTask(toDoTask)

    suspend fun updateCompletedAndTimeUpTasks(completed: Boolean, taskCompletedTime: Date) =
        dao.updateCompletedAndTimeUpTasks(completed, taskCompletedTime)
}