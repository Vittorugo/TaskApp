package com.livrokotlin.taskapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.livrokotlin.taskapp.R
import com.livrokotlin.taskapp.data.model.Status
import com.livrokotlin.taskapp.data.model.Task
import com.livrokotlin.taskapp.databinding.FragmentFormTaskBinding
import com.livrokotlin.taskapp.extensions.initToolbar
import com.livrokotlin.taskapp.extensions.interceptBackPressed
import com.livrokotlin.taskapp.extensions.showBottomSheet

class FormTaskFragment : Fragment() {

    private var _binding: FragmentFormTaskBinding? = null
    private val binding get() = _binding!!
    private lateinit var task: Task
    private var status: Status = Status.TODO
    private var newTask: Boolean = true
    private val args: FormTaskFragmentArgs by navArgs()

    private lateinit var reference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToolbar(binding.toolbar)
        reference = Firebase.database.reference

        // Intercepta o botão de voltar do dispositivo
        interceptBackPressed(R.id.action_formTaskFragment_to_homeFragment)
        getArgs()
        initListeners()
    }

    private fun initListeners() {
        binding.btnSalvar.setOnClickListener {
            validateForm()
        }

        binding.rgStatus.setOnCheckedChangeListener { _, id ->
            status = when(id) {
                R.id.rbTodo -> Status.TODO
                R.id.rbDoing -> Status.DOING
                else -> Status.DONE
            }
        }
    }

    private fun validateForm() {
        val description = binding.editDescription.text.toString().trim()

        if (description.isNotEmpty()) {
            binding.progressBar.isVisible = true

            if (newTask) task = Task()
            task.id  = reference.push().key ?: ""
            task.description = description
            task.status = status

            salveTask(task)
        } else
            showBottomSheet(message = getString(R.string.text_empty_fields))
    }

    private fun salveTask(task: Task) {
        reference
            .child("tasks")
            .child(Firebase.auth.currentUser?.uid ?: "")
            .child(task.id)
            .setValue(task).addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    showBottomSheet(message = getString(R.string.text_accept_description))

                    if (newTask) {
                        findNavController().popBackStack()
                    } else {
                        binding.progressBar.isVisible = false
                    }
                } else {
                    binding.progressBar.isVisible = false
                    showBottomSheet(message = "Erro ao salvar tarefa")
                }

            }
    }

    private fun getArgs() {
        args.task.let {
            if (it != null) {
                this.task = it
                configTask()
            }
        }
    }

    private fun configTask() {
        newTask = false
        status = task.status
        binding.textToolbar.setText(R.string.text_edit_task_toolbar)
        binding.editDescription.setText(task.description)
        setStatus()
    }

    private fun setStatus() {
        val id = when(task.status) {
            Status.TODO -> R.id.rbTodo
            Status.DOING -> R.id.rbDoing
            else -> R.id.rbDone
        }

        binding.rgStatus.check(id)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}