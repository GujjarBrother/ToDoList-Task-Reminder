package com.todo.list.listeners

import com.todo.list.db.ToDoTask

interface ToDoTaskDetailListener {
    fun taskDetail(toDoTask: ToDoTask)
}