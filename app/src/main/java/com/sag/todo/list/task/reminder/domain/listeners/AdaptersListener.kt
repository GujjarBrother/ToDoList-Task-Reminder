package com.sag.todo.list.task.reminder.domain.listeners

interface AdaptersListener<S, A, G> {
    fun itemClicked(
        item: S? = null,
        currentPosition: A? = null,
        previousPosition: G? = null,
        isSwitchChecked: Boolean? = null
    )
}