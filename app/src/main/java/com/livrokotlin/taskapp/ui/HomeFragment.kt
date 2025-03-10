package com.livrokotlin.taskapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.livrokotlin.taskapp.R
import com.livrokotlin.taskapp.databinding.FragmentHomeBinding
import com.livrokotlin.taskapp.extensions.showBottomSheet
import com.livrokotlin.taskapp.ui.adapter.ViewPagerAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth

        initTabs()
        logout()
    }

    private fun initTabs() {
        val pageAdapter = ViewPagerAdapter(requireActivity())
        binding.viewPager.adapter = pageAdapter

        pageAdapter.addFragment(ToDoFragment(), R.string.status_task_todo)
        pageAdapter.addFragment(DoingFragment(), R.string.status_task_doing)
        pageAdapter.addFragment(DoneFragment(), R.string.status_task_done)

        binding.viewPager.offscreenPageLimit = pageAdapter.itemCount

        TabLayoutMediator(
            binding.tabs, binding.viewPager
        ) { tab, position ->
            tab.text = getString(pageAdapter.getTitle(position))
        }.attach()
    }

    private fun logout() {
        binding.btnLogout.setOnClickListener {
            showBottomSheet(
                title = R.string.txt_title_dialog_confirm_logout,
                textBottom = R.string.txt_btn_dialog_confirm_logout,
                message = getString(R.string.txt_message_dialog_confirm_logout),
                onClick = {
                    auth.signOut()
                    findNavController().navigate(R.id.action_homeFragment_to_authentication)
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}