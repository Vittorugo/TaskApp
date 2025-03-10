package com.livrokotlin.taskapp.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.livrokotlin.taskapp.R
import com.livrokotlin.taskapp.databinding.FragmentLoginBinding
import com.livrokotlin.taskapp.extensions.showBottomSheet

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    //private lateinit var auth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //auth = Firebase.auth
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
            binding.progressBar.isVisible = true
            login(email, password)
        }
    }

    private fun login(email: String, password: String) {
        Firebase.auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    findNavController().navigate(R.id.action_loginFragment2_to_homeFragment)
                } else {
                    binding.progressBar.isVisible = false
                    Toast.makeText(requireContext(), task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}