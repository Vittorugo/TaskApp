package com.livrokotlin.taskapp

import android.util.Log
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.livrokotlin.taskapp.data.model.Status
import com.livrokotlin.taskapp.data.model.Task
import com.livrokotlin.taskapp.util.FirebaseHelper

class TaskViewModel : ViewModel() {

    private val _taskList = MutableLiveData<List<Task>>()
    val taskList: LiveData<List<Task>> = _taskList

    private val _taskInsert = MutableLiveData<Task>()
    val taskInsert: LiveData<Task> = _taskInsert

    private val _taskUpdate = MutableLiveData<Task>()
    val taskUpdate: LiveData<Task> = _taskUpdate

    fun setUpdateTask(task: Task) {
        _taskUpdate.postValue(task)
    }

    fun getTasks(status: Status) {
        FirebaseHelper.getDatabase()
            .child(FirebaseHelper.DATABASE_NAME)
            .child(FirebaseHelper.getIdUser())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var tasks = mutableListOf<Task>()
                    Log.d("FirebaseData", "Snapshot: ${snapshot.value}")
                    for ( dataSnapshot in snapshot.children) {
                        Log.d("FirebaseData", "DataSnapshot: ${dataSnapshot.getValue(Task::class.java) as Task}")
                        val task = dataSnapshot.getValue(Task::class.java) as Task
                        if(task.status == status)
                            tasks.add(task)
                    }

                    tasks.reverse()
                    _taskList.postValue(tasks)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
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