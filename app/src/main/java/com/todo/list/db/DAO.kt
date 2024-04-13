package com.todo.list.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface DAO {
    @Insert
    fun saveTask(toDoTask: ToDoTask): Long

    @Query("SELECT * FROM ToDosTasks")
    fun getAllTasks(): List<ToDoTask>

    @Query("SELECT * FROM ToDosTasks WHERE Tasks_Category = :category")
    fun getAllSpecificTasks(category: Int): List<ToDoTask>

    @Delete
    fun deleteTask(toDoTask: ToDoTask): Int

    @Update
    fun updateTask(toDoTask: ToDoTask): Int

    @Query("SELECT COUNT(*) FROM ToDosTasks WHERE Week_Days = :dayOfWeek AND Month_Dates = :date AND Month_Names = :month AND Years = :year AND Tasks_Titles = :title AND Tasks_Descriptions = :description AND Tasks_Time = :time AND Tasks_Category = :category")
    fun isTaskAlreadySaved(
        dayOfWeek: String,
        date: String,
        month: String,
        year: String,
        title: String,
        description: String,
        time: String,
        category: Int
    ): Int
}