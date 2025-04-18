package com.livrokotlin.taskapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.livrokotlin.taskapp.R
import com.livrokotlin.taskapp.TaskViewModel
import com.livrokotlin.taskapp.data.model.Status
import com.livrokotlin.taskapp.data.model.Task
import com.livrokotlin.taskapp.databinding.FragmentFormTaskBinding
import com.livrokotlin.taskapp.util.initToolbar
import com.livrokotlin.taskapp.util.interceptBackPressed
import com.livrokotlin.taskapp.util.showBottomSheet

class FormTaskFragment : BaseFragment() {

    private var _binding: FragmentFormTaskBinding? = null
    private val binding get() = _binding!!
    private lateinit var task: Task
    private var status: Status = Status.TODO
    private var newTask: Boolean = true
    private val args: FormTaskFragmentArgs by navArgs()
    private val viewModel: TaskViewModel by activityViewModels()

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

        // Intercepta o botão de voltar do dispositivo
        interceptBackPressed(R.id.action_formTaskFragment_to_homeFragment)
        getArgs()
        initListeners()
    }

    private fun initListeners() {
        binding.btnSalvar.setOnClickListener {
            observeViewModel()
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
            hideKeyboard()
            binding.progressBar.isVisible = true

            if (newTask)  {
                task = Task()
            }
            task.description = description
            task.status = status

            if (newTask) {
                viewModel.insertTask(task)
            } else {
                viewModel.updateTask(task)
            }

        } else
            showBottomSheet(message = getString(R.string.text_empty_fields))
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

    private fun observeViewModel() {
        viewModel.taskInsert.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(),
                R.string.text_accept_description,
                Toast.LENGTH_SHORT).show()

            findNavController().popBackStack()
        }

        viewModel.taskUpdate.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(),
                R.string.text_edit_task_success,
                Toast.LENGTH_SHORT).show()

            binding.progressBar.isVisible = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}