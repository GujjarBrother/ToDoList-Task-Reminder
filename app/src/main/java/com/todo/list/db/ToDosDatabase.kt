package com.todo.list.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ToDoTask::class], version = 1, exportSchema = false)
abstract class ToDosDatabase : RoomDatabase() {
    abstract fun dao(): DAO
}
