package com.livrokotlin.taskapp.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.livrokotlin.taskapp.R
import com.livrokotlin.taskapp.databinding.FragmentRecoverAccountBinding
import com.livrokotlin.taskapp.ui.BaseFragment
import com.livrokotlin.taskapp.util.FirebaseHelper
import com.livrokotlin.taskapp.util.initToolbar
import com.livrokotlin.taskapp.util.interceptBackPressed
import com.livrokotlin.taskapp.util.showBottomSheet

class RecoverAccountFragment : BaseFragment() {
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

        if (email.isNotBlank()) {
            hideKeyboard()
            binding.progressBar.isVisible = true
            recoverAccount(email)
        } else
            showBottomSheet(message = getString(R.string.text_error_email))
    }

    private fun recoverAccount(email: String) {
        FirebaseHelper.getAuth().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                binding.progressBar.isVisible = true
                if (task.isSuccessful) {
                    showBottomSheet(message = getString(R.string.txt_recover_account), onClick = {
                        binding.progressBar.isVisible = false
                    })
                } else {
                    showBottomSheet(
                        message = getString(FirebaseHelper.validError(task.exception?.message.toString()))
                    )
                    binding.progressBar.isVisible = false
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}