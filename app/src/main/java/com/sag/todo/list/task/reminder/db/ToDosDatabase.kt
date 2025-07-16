package com.sag.todo.list.task.reminder.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sag.todo.list.task.reminder.converters.DateConverter

@Database(entities = [ToDoTask::class], version = 2, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class ToDosDatabase : RoomDatabase() {
    abstract fun dao(): DAO

    companion object {
        val migration1to2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ToDosTasks ADD COLUMN Date_And_Time_In_Millis INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE ToDosTasks ADD COLUMN Is_Task_Completed_OR_TimeUp INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
