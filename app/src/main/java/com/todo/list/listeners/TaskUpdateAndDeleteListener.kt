package com.todo.list.listeners

import android.view.View
import com.todo.list.db.ToDoTask

interface TaskUpdateAndDeleteListener {
    fun taskUpdateAndDelete(toDoTask: ToDoTask, view: View, color: Int, position: Int)
}