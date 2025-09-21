package com.sag.todo.list.task.reminder.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sag.todo.list.task.reminder.models.ToDoTask
import java.util.Date

@Dao
interface DAO {

    @Insert
    suspend fun saveTask(toDoTask: ToDoTask): Long

    @Query("SELECT * FROM ToDosTasks WHERE Is_Task_Completed_OR_TimeUp = :isTaskComplete")
    fun getAllTasks(isTaskComplete: Boolean): LiveData<List<ToDoTask>>

    @Delete
    suspend fun deleteTask(toDoTask: ToDoTask): Int

    @Update
    suspend fun updateTask(toDoTask: ToDoTask): Int

    @Query("SELECT COUNT(*) FROM ToDosTasks WHERE Week_Days = :dayOfWeek AND Month_Dates = :date AND Month_Names = :month AND Years = :year AND Tasks_Titles = :title AND Tasks_Descriptions = :description AND Tasks_Time = :time AND Tasks_Category = :category")
    suspend fun isTaskAlreadySaved(
        dayOfWeek: String,
        date: String,
        month: String,
        year: String,
        title: String,
        description: String,
        time: String,
        category: Int
    ): Int

    @Query("UPDATE ToDosTasks SET Is_Task_Completed_OR_TimeUp = :completed WHERE Date_And_Time_In_Millis <= :taskCompletedTime")
    suspend fun updateCompletedAndTimeUpTasks(completed: Boolean, taskCompletedTime: Date)
}