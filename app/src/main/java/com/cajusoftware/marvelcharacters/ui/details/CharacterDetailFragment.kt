package com.cajusoftware.marvelcharacters.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.cajusoftware.marvelcharacters.databinding.FragmentCharacterDetailBinding
import org.koin.android.ext.android.inject

class CharacterDetailFragment : Fragment() {

    private val args: CharacterDetailFragmentArgs by navArgs()

    private lateinit var binding: FragmentCharacterDetailBinding

    private val viewModel: CharacterDetailViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCharacterDetailBinding.inflate(inflater)
        viewModel.fetchCharacter(args.itemId)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        setImageOnClickListener()
        return binding.root
    }

    private fun setImageOnClickListener() {
        binding.characterImage.setOnClickListener {
            viewModel.character.observe(viewLifecycleOwner) {
                ImageDialogFragment(it?.thumbnail?.thumbnailUri)
                    .show(childFragmentManager, ImageDialogFragment.TAG)
            }
        }
    }

}