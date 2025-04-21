package com.livrokotlin.taskapp.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.livrokotlin.taskapp.R
import com.livrokotlin.taskapp.data.model.Status
import com.livrokotlin.taskapp.data.model.Task
import com.livrokotlin.taskapp.databinding.ItemTaskBinding

class TaskAdapter(
    private val taskSelected: (Task, Int) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(DIFF_CALLBACK){

    companion object {
        const val SELECT_BACK: Int = 1
        const val SELECT_REMOVE: Int = 2
        const val SELECT_EDIT: Int = 3
        const val SELECT_DETAILS: Int = 4
        const val SELECT_NEXT: Int = 5

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem.id == newItem.id && oldItem.description == newItem.description
            }

            override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem == newItem && oldItem.description == newItem.description
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(
            ItemTaskBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false))
    }


    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.binding.textDescription.text = task.description

        setIndicators(task, holder)

        holder.binding.btnDelete.setOnClickListener { taskSelected(task, SELECT_REMOVE) }
        holder.binding.btnEdit.setOnClickListener { taskSelected(task, SELECT_EDIT) }
        holder.binding.btnDetails.setOnClickListener { taskSelected(task, SELECT_DETAILS) }
    }

    private fun setIndicators(task: Task, holder: TaskViewHolder) {
        when ( task.status) {
            Status.TODO -> {
                holder.binding.btnBack.isVisible = false
                holder.binding.btnNext.setColorFilter(
                    ContextCompat.getColor(holder.itemView.context, R.color.color_details)
                )

                holder.binding.btnNext.setOnClickListener { taskSelected(task, SELECT_NEXT) }
            }

            Status.DOING -> {
                Log.d("TaskAdapter", "setIndicators: DOING")
                holder.binding.btnBack.setColorFilter(
                    ContextCompat.getColor(holder.itemView.context, R.color.color_delete))
                holder.binding.btnNext.setColorFilter(
                    ContextCompat.getColor(holder.itemView.context, R.color.color_details))

                holder.binding.btnBack.setOnClickListener { taskSelected(task, SELECT_BACK) }
                holder.binding.btnNext.setOnClickListener { taskSelected(task, SELECT_NEXT) }
            }

            Status.DONE -> {
                holder.binding.btnNext.isVisible = false
                holder.binding.btnBack.setColorFilter(
                    ContextCompat.getColor(holder.itemView.context, R.color.color_delete))

                holder.binding.btnBack.setOnClickListener { taskSelected(task, SELECT_BACK) }
            }
        }
    }

    inner class TaskViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)
}
