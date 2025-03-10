package com.livrokotlin.taskapp.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.livrokotlin.taskapp.R
import com.livrokotlin.taskapp.databinding.FragmentRecoverAccountBinding
import com.livrokotlin.taskapp.extensions.initToolbar
import com.livrokotlin.taskapp.extensions.interceptBackPressed
import com.livrokotlin.taskapp.extensions.showBottomSheet

class RecoverAccountFragment : Fragment() {
    private var _binding: FragmentRecoverAccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecoverAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()
        initToolbar(binding.toolbar)

        // Intercepta o botão de voltar do dispositivo
        interceptBackPressed(R.id.action_recoverAccountFragment_to_loginFragment2)

    }

    private fun initListeners() {
        binding.btnRecover.setOnClickListener {
            validateRecover()
        }
    }

    private fun validateRecover() {
        val email = binding.editEmail.text.toString().trim()

        if (email.isNotBlank())
            showBottomSheet(message = getString(R.string.text_accept_email))
        else
            showBottomSheet(message = getString(R.string.text_error_email))
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}