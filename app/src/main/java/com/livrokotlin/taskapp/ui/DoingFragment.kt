package com.livrokotlin.taskapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.livrokotlin.taskapp.R
import com.livrokotlin.taskapp.TaskViewModel
import com.livrokotlin.taskapp.data.model.Status
import com.livrokotlin.taskapp.data.model.Task
import com.livrokotlin.taskapp.databinding.FragmentDoingBinding
import com.livrokotlin.taskapp.ui.adapter.TaskAdapter
import com.livrokotlin.taskapp.util.StateView
import com.livrokotlin.taskapp.util.showBottomSheet

class DoingFragment : Fragment() {

    private var _binding: FragmentDoingBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter

    private val viewModel: TaskViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerViewTask()
        observeViewModel()
        viewModel.getTasks()
    }

    private fun initRecyclerViewTask() {
        // Outra forma de passar uma função lambda no construtor.
        // Use quando você já tem uma função definida que faz o que você precisa e não quer criar uma lambda nova.
        taskAdapter = TaskAdapter(::optionSelected)

        with(binding.rvTasks) {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = taskAdapter

        }
    }

    private fun optionSelected(task: Task, option: Int) {
        when (option) {
            TaskAdapter.SELECT_BACK -> {
                task.status = Status.TODO
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
                var action = HomeFragmentDirections.actionHomeFragmentToFormTaskFragment(task)
                findNavController().navigate(action)
            }

            TaskAdapter.SELECT_DETAILS -> {
                Toast.makeText(requireContext(), "Detalhes ${task.description}", Toast.LENGTH_SHORT).show()
            }

            TaskAdapter.SELECT_NEXT -> {
                task.status = Status.DONE
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
//        if (tasks.isEmpty()) {
//            binding.txtLoading.text = getString(R.string.txt_task_list_empty)
//        } else {
//            binding.txtLoading.text = ""
//        }
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
                    val tasks = stateView.data?.filter { it.status == Status.DOING } ?: emptyList()

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
                    //TODO("FALTA IMPLEMENTAR O PROGRESSBAR")
                }

                is StateView.OnSuccess -> {
                    val oldList = taskAdapter.currentList
                    val newList = oldList.toMutableList().apply {
                        if (stateView.data?.status == Status.DOING) {
                            val index = oldList.indexOfFirst { it.id == stateView.data.id }
                            if (index == -1) {
                                add(0, stateView.data)
                                binding.rvTasks.smoothScrollToPosition(0)
                            } else {
                                set(index, stateView.data)
                            }
                        } else {
                            remove(stateView.data)
                        }
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

        viewModel.taskDelete.observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.OnLoading -> {
                    //TODO("FALTA IMPLEMENTAR O PROGRESSBAR")
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