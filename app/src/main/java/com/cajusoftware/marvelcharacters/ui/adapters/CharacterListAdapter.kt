package com.cajusoftware.marvelcharacters.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.CombinedLoadStates
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.cajusoftware.marvelcharacters.R
import com.cajusoftware.marvelcharacters.data.domain.CarouselCharacter
import com.cajusoftware.marvelcharacters.data.domain.CharacterModel
import com.cajusoftware.marvelcharacters.data.domain.observers.CarouselCharacterObserver
import com.cajusoftware.marvelcharacters.data.domain.observers.CarouselCharacterSubject
import com.cajusoftware.marvelcharacters.databinding.ItemCarouselCharacterBinding
import com.cajusoftware.marvelcharacters.databinding.ItemTitleBinding
import com.cajusoftware.marvelcharacters.databinding.ItemUpperCarouselBinding

private const val CHARACTER_ITEM = 0
private const val CAROUSEL_ITEM = 1
private const val TITLE_ITEM = 2

class CharacterListAdapter(
    private val carouselCharacterSubject: CarouselCharacterSubject,
    private val onItemClickListener: ((Int) -> Unit),
    private val shouldListeningUpperCarouselItems: () -> Unit
) : PagingDataAdapter<CharacterModel, CharacterListAdapter.CharacterModelViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterModelViewHolder {
        return when (viewType) {
            CHARACTER_ITEM ->
                CharacterViewHolder(
                    ItemCarouselCharacterBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            CAROUSEL_ITEM -> CarouselItem(
                ItemUpperCarouselBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> TitleItem(
                ItemTitleBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: CharacterModelViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CharacterModel.CharacterItem -> CHARACTER_ITEM
            is CharacterModel.CarouselItem -> CAROUSEL_ITEM
            else -> TITLE_ITEM
        }
    }

    fun stateFlow(stateCallback: (CombinedLoadStates) -> Unit) {
        addLoadStateListener(stateCallback)
    }

    inner class CharacterViewHolder(private val binding: ItemCarouselCharacterBinding) :
        CharacterModelViewHolder(binding.root) {

        override fun bind(characterModel: CharacterModel) {
            val character = (characterModel as CharacterModel.CharacterItem).character
            binding.apply {
                characterName.text = character.name
                characterDescription.text = character.copyright

                characterImage.load(character.thumbnail?.thumbnailUri) {
                    placeholder(R.drawable.loading_animation)
                    error(R.drawable.ic_broken_image)
                    memoryCachePolicy(CachePolicy.ENABLED)
                    diskCachePolicy(CachePolicy.ENABLED)
                }

                comicsAmount.text = character.comicsAmount?.toString()
                eventsAmount.text = character.eventsAmount?.toString()
                seriesAmount.text = character.seriesAmount?.toString()
                storiesAmount.text = character.storiesAmount?.toString()

                itemRoot.setOnClickListener { onItemClickListener(character.id) }
            }
        }
    }

    inner class CarouselItem(private val binding: ItemUpperCarouselBinding) :
        CharacterModelViewHolder(binding.root), CarouselCharacterObserver {

        override fun bind(characterModel: CharacterModel) {
            carouselCharacterSubject.attach(this)
            shouldListeningUpperCarouselItems()
        }

        override fun collect(items: List<CarouselCharacter>) {
            binding.apply {
                upperCarouselView.post {
                    upperCarouselView.items = items
                    upperCarouselView.onItemClickListener = onItemClickListener
                }
            }
        }

        fun unbind() {
            carouselCharacterSubject.detach(this)
        }
    }

    inner class TitleItem(private val binding: ItemTitleBinding) :
        CharacterModelViewHolder(binding.root) {

        override fun bind(characterModel: CharacterModel) {
            binding.title.text = (characterModel as? CharacterModel.TitleItem)?.title ?: "Error"
        }
    }

    abstract class CharacterModelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(characterModel: CharacterModel)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<CharacterModel>() {
        override fun areItemsTheSame(
            oldItem: CharacterModel,
            newItem: CharacterModel
        ): Boolean {
            return (oldItem as? CharacterModel.CharacterItem)?.character?.id == (newItem as? CharacterModel.CharacterItem)?.character?.id ||
                    (oldItem as? CharacterModel.TitleItem)?.title == (newItem as? CharacterModel.TitleItem)?.title
        }

        override fun areContentsTheSame(
            oldItem: CharacterModel,
            newItem: CharacterModel
        ): Boolean {
            return oldItem == newItem
        }
    }
}