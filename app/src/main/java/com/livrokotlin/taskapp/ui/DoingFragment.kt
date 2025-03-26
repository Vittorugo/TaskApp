package com.livrokotlin.taskapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
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
import com.livrokotlin.taskapp.databinding.FragmentDoingBinding
import com.livrokotlin.taskapp.ui.adapter.TaskAdapter
import com.livrokotlin.taskapp.util.FirebaseHelper
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
        getTasks()
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
                updateTask(task)
            }

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
                var action = HomeFragmentDirections.actionHomeFragmentToFormTaskFragment(task)
                findNavController().navigate(action)
                observeViewModel()
            }

            TaskAdapter.SELECT_DETAILS -> {
                Toast.makeText(requireContext(), "Detalhes ${task.description}", Toast.LENGTH_SHORT).show()
            }

            TaskAdapter.SELECT_NEXT -> {
                task.status = Status.DONE
                updateTask(task)
            }
        }
    }

    private fun getTasks() {
        FirebaseHelper.getDatabase()
            .child(FirebaseHelper.DATABASE_NAME)
            .child(FirebaseHelper.getIdUser())
            .addValueEventListener(object : ValueEventListener { // Esta linha anexa um ValueEventListener à referência do banco de dados. Um ValueEventListener escuta as mudanças nos dados no local especificado e aciona o método onDataChange sempre que ocorrem mudanças
                override fun onDataChange(snapshot: DataSnapshot) {
                    var tasks = mutableListOf<Task>()
                    for ( dataSnapshot in snapshot.children) {
                        val task = dataSnapshot.getValue(Task::class.java) as Task
                        if(task.status == Status.DOING)
                            tasks.add(task)
                    }

                    listTaskEmpty(tasks)

                    tasks.reverse()
                    taskAdapter.submitList(tasks)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Erro ao buscar tarefas", Toast.LENGTH_SHORT).show()
                }

            })
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

    private fun deleteTask(task: Task) {
        FirebaseHelper.getDatabase()
            .child(FirebaseHelper.DATABASE_NAME)
            .child(FirebaseHelper.getIdUser())
            .child(task.id)
            .removeValue().addOnCompleteListener { result ->
                if(result.isSuccessful) {
                    Toast.makeText(requireContext(), "Tarefa removida com sucesso." , Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Erro ao tentar remover tarefa!" , Toast.LENGTH_SHORT).show()
                }
            }

    }

    private fun observeViewModel() {
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

    private fun updateTask(task: Task) {
        FirebaseHelper.getDatabase()
            .child(FirebaseHelper.DATABASE_NAME)
            .child(FirebaseHelper.getIdUser())
            .child(task.id)
            .setValue(task)
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    Toast.makeText(requireContext(), "Tarefa atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Erro ao atualizar tarefa!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}