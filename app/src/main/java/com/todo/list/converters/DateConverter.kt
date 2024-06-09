package com.todo.list.converters

import androidx.room.TypeConverter
import java.util.Date

class DateConverter {

    @TypeConverter
    fun fromDateToLong(date: Date) = date.time

    @TypeConverter
    fun fromLongToDate(dateInLong: Long) = Date(dateInLong)
}