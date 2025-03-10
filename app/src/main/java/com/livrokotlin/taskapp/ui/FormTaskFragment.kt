package com.livrokotlin.taskapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.livrokotlin.taskapp.R
import com.livrokotlin.taskapp.databinding.FragmentFormTaskBinding
import com.livrokotlin.taskapp.extensions.initToolbar
import com.livrokotlin.taskapp.extensions.interceptBackPressed
import com.livrokotlin.taskapp.extensions.showBottomSheet

class FormTaskFragment : Fragment() {

    private var _binding: FragmentFormTaskBinding? = null
    private val binding get() = _binding!!

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

        initListeners()
    }

    private fun initListeners() {
        binding.btnSalvar.setOnClickListener {
            validateForm()
        }
    }

    private fun validateForm() {
        val description = binding.editDescription.text.toString().trim()

        if (description.isEmpty())
            showBottomSheet(message = getString(R.string.text_empty_fields))
        else
            showBottomSheet(message = getString(R.string.text_accept_description))
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}