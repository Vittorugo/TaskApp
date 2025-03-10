package com.livrokotlin.taskapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.livrokotlin.taskapp.R
import com.livrokotlin.taskapp.data.model.Status
import com.livrokotlin.taskapp.data.model.Task
import com.livrokotlin.taskapp.databinding.FragmentToDoBinding
import com.livrokotlin.taskapp.ui.adapter.TaskAdapter

class ToDoFragment : Fragment() {

    private var _binding: FragmentToDoBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter

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
        taskAdapter.submitList(getTasks())
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
                Toast.makeText(requireContext(), "Removendo ${task.description}", Toast.LENGTH_SHORT).show()
            }

            TaskAdapter.SELECT_EDIT -> {
                Toast.makeText(requireContext(), "Editando ${task.description}", Toast.LENGTH_SHORT).show()
            }

            TaskAdapter.SELECT_DETAILS -> {
                Toast.makeText(requireContext(), "Detalhes ${task.description}", Toast.LENGTH_SHORT).show()
            }

            TaskAdapter.SELECT_NEXT -> {
                Toast.makeText(requireContext(), "Próxima ${task.description}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun getTasks() = listOf(
        Task("01", "Estudar Android", Status.TODO),
        Task("02", "Estudar Kotlin", Status.TODO),
        Task("03", "Estudar Room", Status.TODO),
        Task("04", "Estudar Firebase", Status.TODO)
    )

    private fun addNewTask() {
        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_formTaskFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}