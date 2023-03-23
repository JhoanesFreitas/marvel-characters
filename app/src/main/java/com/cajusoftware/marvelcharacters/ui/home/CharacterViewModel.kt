package com.cajusoftware.marvelcharacters.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.cajusoftware.marvelcharacters.data.domain.CarouselCharacter
import com.cajusoftware.marvelcharacters.data.domain.CharacterModel
import com.cajusoftware.marvelcharacters.data.network.NoConnectivityException
import com.cajusoftware.marvelcharacters.data.repositories.CharacterRepository
import com.cajusoftware.marvelcharacters.utils.NetworkUtils.exceptionHandler
import com.cajusoftware.marvelcharacters.utils.RetryCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted.Companion.Lazily
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class CharacterViewModel(private val characterRepository: CharacterRepository) : ViewModel() {

    private var currentJob: Job? = null

    private val _upperCarouselItems = MutableLiveData<List<CarouselCharacter>>()
    val upperCarouselItems: LiveData<List<CarouselCharacter>>
        get() = _upperCarouselItems

    private val _carouselItems = MutableLiveData<PagingData<CharacterModel>>()
    val carouselItems: LiveData<PagingData<CharacterModel>>
        get() = _carouselItems

    private val _shouldShowPlaceholder = MutableLiveData(true)
    val shouldShowPlaceholder: LiveData<Boolean>
        get() = _shouldShowPlaceholder


    val scope: CoroutineScope
        get() = viewModelScope

    fun getCharactersToUpperCarousel() {
        viewModelScope.launch {
            characterRepository.upperCarouselCharacters.shareIn(this, Lazily).collectLatest {
                _upperCarouselItems.postValue(it)
            }
        }
    }

    fun getCharacters() {
        currentJob?.cancel()
        currentJob = viewModelScope.launch(Dispatchers.IO) {
            characterRepository.carouselCharacters
                .cancellable()
                .shareIn(this, Lazily)
                .cachedIn(viewModelScope)
                .collectLatest {
                    _carouselItems.postValue(it)
                    characterRepository.getCharactersRandomly()
                }
        }
    }

    fun setPlaceholderVisibility(isVisible: Boolean = false) {
        _shouldShowPlaceholder.postValue(isVisible)
    }

    fun checkLoadState(loadState: CombinedLoadStates) {
        loadState.apply {
            (prepend as? LoadState.Error)?.error?.apply {
                (this as? NoConnectivityException)?.let {
                    exceptionHandler.handleException(
                        scope.coroutineContext + RetryCallback { getCharacters() },
                        it
                    )
                }
            }
        }
    }
}