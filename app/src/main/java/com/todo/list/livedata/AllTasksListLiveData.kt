package com.todo.list.livedata

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AllTasksListLiveData : ViewModel() {
    var mutableLiveData = MutableLiveData(false)

    fun setMutableLiveDataValue(value: Boolean) {
        mutableLiveData.value = value
    }
}