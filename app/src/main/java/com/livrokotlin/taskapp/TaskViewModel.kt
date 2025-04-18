package com.livrokotlin.taskapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.livrokotlin.taskapp.data.model.Task
import com.livrokotlin.taskapp.util.FirebaseHelper

class TaskViewModel : ViewModel() {


    private val _taskInsert = MutableLiveData<Task>()
    val taskInsert: LiveData<Task> = _taskInsert

    private val _taskUpdate = MutableLiveData<Task>()
    val taskUpdate: LiveData<Task> = _taskUpdate

    fun setUpdateTask(task: Task) {
        _taskUpdate.postValue(task)
    }

    fun insertTask(task: Task) {
            FirebaseHelper.getDatabase()
                .child(FirebaseHelper.DATABASE_NAME)
                .child(FirebaseHelper.getIdUser())
                .child(task.id)
                .setValue(task).addOnCompleteListener { result ->
                    if (result.isSuccessful) {
                       _taskInsert.postValue(task)
                }
        }
    }
}