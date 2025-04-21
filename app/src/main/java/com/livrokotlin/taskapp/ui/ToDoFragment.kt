package com.livrokotlin.taskapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.livrokotlin.taskapp.R
import com.livrokotlin.taskapp.TaskViewModel
import com.livrokotlin.taskapp.data.model.Status
import com.livrokotlin.taskapp.data.model.Task
import com.livrokotlin.taskapp.databinding.FragmentToDoBinding
import com.livrokotlin.taskapp.ui.adapter.TaskAdapter
import com.livrokotlin.taskapp.util.FirebaseHelper
import com.livrokotlin.taskapp.util.StateView
import com.livrokotlin.taskapp.util.showBottomSheet

class ToDoFragment : Fragment() {

    private var _binding: FragmentToDoBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter

    private val viewModel: TaskViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentToDoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addNewTask()
        initRecyclerViewTask()
        observeViewModel()
        viewModel.getTasks(Status.TODO)
    }

    private fun initRecyclerViewTask() {
        // Abordagem trailing lambda (lambda por último) para passar a função como parâmetro do construtor
        // Use quando a lambda é o último parâmetro e você quer um código conciso e legível.
        // É a opção mais comum e recomendada nesse caso
        taskAdapter = TaskAdapter() { task, option ->
            optionSelected(task, option)
        }

        with(binding.rvTasks) {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = taskAdapter
        }
//
//        Outra forma de fazer a mesma coisa:

//        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
//        binding.rvTasks.setHasFixedSize(true)
//        binding.rvTasks.adapter = taskAdapter
    }

    private fun optionSelected(task: Task, option: Int) {
        when (option) {
            TaskAdapter.SELECT_REMOVE -> {
                showBottomSheet(
                    title = R.string.txt_delete_task,
                    textBottom = R.string.txt_confirm,
                    message = getString(R.string.txt_warning_delete_task),
                    onClick = {
                        viewModel.deleteTask(task)
                    }
                )
            }

            TaskAdapter.SELECT_EDIT -> {
                val action = HomeFragmentDirections.actionHomeFragmentToFormTaskFragment(task)
                findNavController().navigate(action)
            }

            TaskAdapter.SELECT_DETAILS -> {
                Toast.makeText(requireContext(), "Detalhes ${task.description}", Toast.LENGTH_SHORT).show()
            }

            TaskAdapter.SELECT_NEXT -> {
                task.status = Status.DOING
                viewModel.updateTask(task)
            }
        }
    }

    private fun listTaskEmpty(tasks: List<Task>) {
        binding.txtLoading.text = if(tasks.isEmpty()) {
            getString(R.string.txt_task_list_empty)
        } else {
            ""
        }
    }

    private fun addNewTask() {
        binding.floatingActionButton.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToFormTaskFragment(null)
            findNavController().navigate(action)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.taskList.observe(viewLifecycleOwner) { stateView ->

            when(stateView) {
                is StateView.OnLoading -> {
                    binding.progressBar.isVisible = true
                }

                is StateView.OnSuccess -> {
                    binding.progressBar.isVisible = false
                    val tasks = stateView.data ?: emptyList()

                    listTaskEmpty(tasks)

                    taskAdapter.submitList(tasks)
                }

                is StateView.OnError -> {
                    Toast.makeText(requireContext(),
                        stateView.message,
                        Toast.LENGTH_SHORT).show()

                    binding.progressBar.isVisible = false
                }

            }
        }

        viewModel.taskInsert.observe(viewLifecycleOwner) { stateView ->

            when (stateView) {
                is StateView.OnLoading -> {
                    binding.progressBar.isVisible = true
                }

                is StateView.OnSuccess -> {
                    binding.progressBar.isVisible = false

                    if(stateView.data?.status == Status.TODO) {
                        val oldList = taskAdapter.currentList

                        val newList = oldList.toMutableList().apply {
                            add(0, stateView.data)
                        }

                        taskAdapter.submitList(newList)

                        // Quando voltar para tela do 'TO DO' a lista do recyclerView volta para o topo
                        binding.rvTasks.smoothScrollToPosition(0)
                    }
                }

                is StateView.OnError -> {
                    Toast.makeText(requireContext(),
                        stateView.message,
                        Toast.LENGTH_SHORT).show()

                    binding.progressBar.isVisible = false
                }
            }

        }

        viewModel.taskUpdate.observe(viewLifecycleOwner) { stateView ->

            when (stateView) {
                is StateView.OnLoading -> {
                    binding.progressBar.isVisible = true
                }

                is StateView.OnSuccess -> {
                    binding.progressBar.isVisible = false

                    // Armazena a lista atual do adapter
                    val oldList = taskAdapter.currentList

                    // Gera uma nova lista a partir da lista antiga já com a tarefa atualizada
                    val newList = oldList.toMutableList().apply {
                        // Condição para validar se o usuario alterou apenas a descrição da tarefa ou o status tbm.
                        // Caso tenha alterado o status, remove a tarefa da lista
                        if(stateView.data?.status == Status.TODO) {
                            find { it.id == stateView.data.id }?.description = stateView.data.description
                        } else {
                            remove(stateView.data)
                        }
                    }

                    // Armazena a posição da tarefa a ser atualziada na lista
                    val position = oldList.indexOfFirst { it.id == stateView.data?.id }

                    // Envia a lista atualizada para o adapter
                    taskAdapter.submitList(newList)

                    // Atualiza a tarefa pela posição do adapter
                    taskAdapter.notifyItemChanged(position)
                }

                is StateView.OnError -> {
                    Toast.makeText(requireContext(),
                        stateView.message,
                        Toast.LENGTH_SHORT).show()

                    binding.progressBar.isVisible = false
                }
            }
        }

        viewModel.taskDelete.observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.OnLoading -> {
                    binding.progressBar.isVisible = true
                }

                is StateView.OnSuccess -> {
                    Toast.makeText(requireContext(),
                        getString(R.string.text_delete_task_success),
                        Toast.LENGTH_SHORT).show()

                    val oldList = taskAdapter.currentList
                    val newList = oldList.toMutableList().apply {
                        remove(stateView.data)
                    }
                    taskAdapter.submitList(newList)
                }

                is StateView.OnError -> {
                    Toast.makeText(requireContext(),
                        stateView.message,
                        Toast.LENGTH_SHORT).show()

                    binding.progressBar.isVisible = false
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}