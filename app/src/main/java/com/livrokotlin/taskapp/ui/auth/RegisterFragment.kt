package com.livrokotlin.taskapp.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.livrokotlin.taskapp.R
import com.livrokotlin.taskapp.databinding.FragmentRegisterBinding
import com.livrokotlin.taskapp.extensions.initToolbar
import com.livrokotlin.taskapp.extensions.interceptBackPressed
import com.livrokotlin.taskapp.extensions.showBottomSheet

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

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
        auth = Firebase.auth

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
                binding.progressBar.isVisible = true
                registerUser(email, password)
            } else
                showBottomSheet(message = getString(R.string.text_error_pass))
        } else {
            showBottomSheet(message = getString(R.string.text_error_email))
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    findNavController().navigate(R.id.action_loginFragment2_to_homeFragment)
                } else {
                    binding.progressBar.isVisible = false
                    Toast.makeText(requireContext(),
                        task.exception?.message,
                        Toast.LENGTH_SHORT)
                        .show()
                }

            }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}