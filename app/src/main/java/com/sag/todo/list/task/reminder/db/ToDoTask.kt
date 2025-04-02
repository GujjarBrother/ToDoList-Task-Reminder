package com.sag.todo.list.task.reminder.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.Date

@Entity(tableName = "ToDosTasks")
data class ToDoTask(
        @PrimaryKey(autoGenerate = true)
        var id: Int,

        @ColumnInfo(name = "Week_Days")
        val day: String,

        @ColumnInfo(name = "Month_Dates")
        val date: String,

        @ColumnInfo(name = "Month_Names")
        val month: String,

        @ColumnInfo(name = "Years")
        val year: String,

        @ColumnInfo(name = "Tasks_Titles")
        val title: String,

        @ColumnInfo(name = "Tasks_Descriptions")
        val description: String,

        @ColumnInfo(name = "Tasks_Time")
        val time: String,

        @ColumnInfo(name = "Tasks_Category")
        val category: Int,

        @ColumnInfo(name = "Date_And_Time_In_Millis")
        val dateAndTimeInMillis: Date,

        @ColumnInfo(name = "Is_Task_Completed_OR_TimeUp")
        val isTaskCompletedORTimeUp: Boolean
) : Serializable