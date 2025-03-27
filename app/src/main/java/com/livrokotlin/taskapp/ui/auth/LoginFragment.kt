package com.livrokotlin.taskapp.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.livrokotlin.taskapp.R
import com.livrokotlin.taskapp.databinding.FragmentLoginBinding
import com.livrokotlin.taskapp.ui.BaseFragment
import com.livrokotlin.taskapp.util.FirebaseHelper
import com.livrokotlin.taskapp.util.showBottomSheet

class LoginFragment : BaseFragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        binding.btnLogin.setOnClickListener {
            validateLogin()
        }

        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment2_to_registerFragment)
        }

        binding.btnRecover.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment2_to_recoverAccountFragment)
        }
    }

    private fun validateLogin() {
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showBottomSheet(message = getString(R.string.text_empty_fields))
        } else {
            hideKeyboard()
            binding.progressBar.isVisible = true
            login(email, password)
        }
    }

    private fun login(email: String, password: String) {
        FirebaseHelper.getAuth().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    findNavController().navigate(R.id.action_loginFragment2_to_homeFragment)
                } else {
                    Log.i("TAG", "Login error: ${task.exception?.message}")
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