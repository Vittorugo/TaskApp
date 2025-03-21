package com.livrokotlin.taskapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.livrokotlin.taskapp.R
import com.livrokotlin.taskapp.TaskViewModel
import com.livrokotlin.taskapp.data.model.Status
import com.livrokotlin.taskapp.data.model.Task
import com.livrokotlin.taskapp.databinding.FragmentToDoBinding
import com.livrokotlin.taskapp.extensions.showBottomSheet
import com.livrokotlin.taskapp.ui.adapter.TaskAdapter

class ToDoFragment : Fragment() {

    private var _binding: FragmentToDoBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter

    private lateinit var reference: DatabaseReference
    private lateinit var auth: FirebaseAuth

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

        reference = Firebase.database.reference
        auth = Firebase.auth

        addNewTask()
        initRecyclerViewTask()
        getTasks()
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
                        deleteTask(task)
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
                Toast.makeText(requireContext(), "Próxima ${task.description}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getTasks() {
        reference
            .child("tasks")
            .child(auth.currentUser?.uid ?: "")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var tasks = mutableListOf<Task>()
                    for ( dataSnapshot in snapshot.children) {
                        val task = dataSnapshot.getValue(Task::class.java) as Task
                        if(task.status == Status.TODO)
                            tasks.add(task)
                    }

                    binding.progressBar.isVisible = false
                    listTaskEmpty(tasks)

                    tasks.reverse()
                    taskAdapter.submitList(tasks)
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.isVisible = false
                    Toast.makeText(requireContext(), "Erro ao buscar tarefas", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun deleteTask(task: Task) {
        reference
            .child("tasks")
            .child(auth.currentUser?.uid ?: "")
            .child(task.id)
            .removeValue().addOnCompleteListener { result ->
                if(result.isSuccessful) {
                    Toast.makeText(requireContext(), "Tarefa removida com sucesso", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Erro ao remover tarefa", Toast.LENGTH_SHORT).show()
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
        Log.i("TAG", "observeViewModel")
        viewModel.taskUpdate.observe(viewLifecycleOwner) { updateTask ->
            if(updateTask.status == Status.TODO) {

                // Armazena a lista atual do adapter
                val oldList = taskAdapter.currentList

                // Gera uma nova lista a partir da lista antiga já com a tarefa atualizada
                val newList = oldList.toMutableList().apply {
                    find { it.id == updateTask.id }?.description = updateTask.description
                }

                // Armazena a posição da tarefa a ser atualziada na lista
                val position = oldList.indexOfFirst { it.id == updateTask.id }

                // Envia a lista atualizada para o adapter
                taskAdapter.submitList(newList)

                // Atualiza a tarefa pela posição do adapter
                taskAdapter.notifyItemChanged(position)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}