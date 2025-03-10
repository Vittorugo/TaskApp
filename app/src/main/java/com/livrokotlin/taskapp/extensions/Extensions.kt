package com.livrokotlin.taskapp.extensions

import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.livrokotlin.taskapp.R
import com.livrokotlin.taskapp.databinding.BottomSheetBinding

fun Fragment.initToolbar(toolbar: androidx.appcompat.widget.Toolbar) {
    (activity as AppCompatActivity).setSupportActionBar(toolbar)
    (activity as AppCompatActivity).title = ""
    (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    toolbar.setNavigationOnClickListener {
        Log.d("Toolbar", "Botão de voltar clicado")
        if (!findNavController().popBackStack()) {
            Log.d("Toolbar", "Pilha de retorno vazia, finalizando atividade")
            requireActivity().finish()
        } else {
            Log.d("Toolbar", "Navegando para trás")
        }
    }
}

fun Fragment.interceptBackPressed(action: Int) {
    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
        OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            findNavController().navigate(action)
        }
    })
}

fun Fragment.showBottomSheet(
    title: Int? = null,
    textBottom: Int? = null,
    message: String,
    onClick: () -> Unit = {}
) {

    val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
    val binding: BottomSheetBinding = BottomSheetBinding.inflate(layoutInflater, null, false)

    binding.txtTitle.text = getText( title ?: R.string.title_bottom_sheet)
    binding.txtMessage.text = message
    binding.buttomAgree.text = getText( textBottom ?: R.string.text_bottom_sheet)
    binding.buttomAgree.setOnClickListener {
        onClick()
        bottomSheetDialog.dismiss()
    }

    bottomSheetDialog.setContentView(binding.root)
    bottomSheetDialog.show()
}