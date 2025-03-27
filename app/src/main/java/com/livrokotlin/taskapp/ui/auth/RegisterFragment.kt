package com.livrokotlin.taskapp.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.livrokotlin.taskapp.R
import com.livrokotlin.taskapp.databinding.FragmentRegisterBinding
import com.livrokotlin.taskapp.ui.BaseFragment
import com.livrokotlin.taskapp.util.FirebaseHelper
import com.livrokotlin.taskapp.util.initToolbar
import com.livrokotlin.taskapp.util.interceptBackPressed
import com.livrokotlin.taskapp.util.showBottomSheet

class RegisterFragment : BaseFragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToolbar(binding.toolbar)

        initListeners()

        // Intercepta o botão de voltar do dispositivo
        interceptBackPressed(R.id.action_registerFragment_to_loginFragment2)
    }

    private fun initListeners() {
        binding.btnRegister.setOnClickListener {
            validateRegister()
        }
    }

    private fun validateRegister() {
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()

        if(email.isNotEmpty()) {
            if(password.isNotEmpty()) {
                hideKeyboard()
                binding.progressBar.isVisible = true
                registerUser(email, password)
            } else
                showBottomSheet(message = getString(R.string.text_error_pass))
        } else {
            showBottomSheet(message = getString(R.string.text_error_email))
        }
    }

    private fun registerUser(email: String, password: String) {
        FirebaseHelper.getAuth().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    findNavController().navigate(R.id.action_loginFragment2_to_homeFragment)
                } else {
                    binding.progressBar.isVisible = false
                    showBottomSheet(
                        message = getString(FirebaseHelper.validError(task.exception?.message.toString()))
                    )
                }

            }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}