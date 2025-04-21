package com.livrokotlin.taskapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.livrokotlin.taskapp.R
import com.livrokotlin.taskapp.TaskViewModel
import com.livrokotlin.taskapp.data.model.Status
import com.livrokotlin.taskapp.data.model.Task
import com.livrokotlin.taskapp.databinding.FragmentDoneBinding
import com.livrokotlin.taskapp.ui.adapter.TaskAdapter
import com.livrokotlin.taskapp.util.FirebaseHelper
import com.livrokotlin.taskapp.util.StateView
import com.livrokotlin.taskapp.util.showBottomSheet

class DoneFragment : Fragment() {

    private var _binding: FragmentDoneBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter

    private val viewModel: TaskViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDoneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerViewTask()
        viewModel.getTasks()
        observeViewModel()
    }

    private fun initRecyclerViewTask() {
        // Outra abordagem de como podemos passar uma função lambda no construtor
        // Use quando a lambda não é o último parâmetro ou quando você prefere uma sintaxe mais explícita.
        taskAdapter = TaskAdapter(taskSelected = { task, option ->
            optionSelected(task, option)
        })
        with(binding.rvTasks) {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = taskAdapter

        }

    }

    private fun optionSelected(task: Task, option: Int) {
        when (option) {
            TaskAdapter.SELECT_BACK -> {
                task.status = Status.DOING
                viewModel.updateTask(task)
            }

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
        }
    }

    private fun listTaskEmpty(tasks: List<Task>) {
        binding.txtLoading.text = if (tasks.isEmpty()) {
            getString(R.string.txt_task_list_empty)
        } else {
            ""
        }
    }

    private fun observeViewModel() {
        viewModel.taskList.observe(viewLifecycleOwner) { stateView ->

            when(stateView) {
                is StateView.OnLoading -> {
                    Toast.makeText(requireContext(),
                        "Carregando tarefas...",
                        Toast.LENGTH_SHORT).show()
                }

                is StateView.OnSuccess -> {
                    val tasks = stateView.data?.filter { it.status == Status.DONE } ?: emptyList()

                    listTaskEmpty(tasks)

                    taskAdapter.submitList(tasks)
                }

                is StateView.OnError -> {
                    Toast.makeText(requireContext(),
                        stateView.message,
                        Toast.LENGTH_SHORT).show()
                }

            }
        }

        viewModel.taskUpdate.observe(viewLifecycleOwner) { stateView ->

            when (stateView) {
                is StateView.OnLoading -> {
                    Toast.makeText(requireContext(),
                        "Carregando tarefas...",
                        Toast.LENGTH_SHORT).show()
                }

                is StateView.OnSuccess -> {

                    // Armazena a lista atual do adapter
                    val oldList = taskAdapter.currentList

                    // Gera uma nova lista a partir da lista antiga já com a tarefa atualizada
                    val newList = oldList.toMutableList().apply {
                        if(!oldList.contains(stateView.data) && stateView.data?.status == Status.DONE) {
                            add(0, stateView.data)
                            binding.rvTasks.smoothScrollToPosition(0)
                        }

                        // Condição para validar se o usuario alterou apenas a descrição da tarefa ou o status tbm.
                        // Caso tenha alterado o status, remove a tarefa da lista
                        if(stateView.data?.status == Status.DONE) {
                            find { it.id == stateView.data.id }?.description = stateView.data.description
                        } else {
                            remove(stateView.data)
                        }
                    }

                    // Armazena a posição da tarefa a ser atualziada na lista
                    val position = oldList.indexOfFirst { it.id == stateView.data?.id }

                    // Envia a lista atualizada para o adapter
                    listTaskEmpty(newList)
                    taskAdapter.submitList(newList)

                    // Atualiza a tarefa pela posição do adapter
                    taskAdapter.notifyItemChanged(position)
                }

                is StateView.OnError -> {
                    Toast.makeText(requireContext(),
                        stateView.message,
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.taskDelete.observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.OnLoading -> {
                    Toast.makeText(requireContext(),
                        "Carregando tarefas...",
                        Toast.LENGTH_SHORT).show()
                }

                is StateView.OnSuccess -> {
                    Toast.makeText(requireContext(),
                        getString(R.string.text_delete_task_success),
                        Toast.LENGTH_SHORT).show()

                    val oldList = taskAdapter.currentList
                    val newList = oldList.toMutableList().apply {
                        remove(stateView.data)
                    }

                    listTaskEmpty(newList)
                    taskAdapter.submitList(newList)
                }

                is StateView.OnError -> {
                    Toast.makeText(requireContext(),
                        stateView.message,
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}