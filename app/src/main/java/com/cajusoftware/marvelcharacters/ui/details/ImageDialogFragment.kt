package com.cajusoftware.marvelcharacters.ui.details

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.transition.TransitionInflater
import com.cajusoftware.marvelcharacters.databinding.FragmentImageBinding

class ImageDialogFragment(private val uri: Uri?) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = FragmentImageBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())

        sharedElementEnterTransition =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)

        sharedElementReturnTransition =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)

        binding.uri = uri
        dialog.setContentView(binding.root)
        val window = dialog.window
        window?.setLayout(900, 1200)

        return dialog
    }

    companion object {
        const val TAG = "ImageDialogFragment"
    }
}