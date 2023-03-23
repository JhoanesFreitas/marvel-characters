package com.cajusoftware.marvelcharacters.data.repositories

import android.content.res.Resources
import androidx.paging.*
import com.cajusoftware.marvelcharacters.BuildConfig.PAGE_PREFETCH_DISTANCE
import com.cajusoftware.marvelcharacters.BuildConfig.PAGE_SIZE
import com.cajusoftware.marvelcharacters.R
import com.cajusoftware.marvelcharacters.data.database.dao.CharacterDao
import com.cajusoftware.marvelcharacters.data.database.sources.ModelsPagingMediator
import com.cajusoftware.marvelcharacters.data.database.sources.ModelsPagingSource
import com.cajusoftware.marvelcharacters.data.domain.CarouselCharacter
import com.cajusoftware.marvelcharacters.data.domain.Character
import com.cajusoftware.marvelcharacters.data.domain.CharacterModel
import com.cajusoftware.marvelcharacters.utils.asCarouselCharacter
import com.cajusoftware.marvelcharacters.utils.asCharacter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class CharacterRepositoryImpl(
    private val resources: Resources,
    private val characterDao: CharacterDao,
    private val remoteMediator: ModelsPagingMediator,
    private val ioDispatcher: CoroutineDispatcher
) : CharacterRepository {

    @OptIn(ExperimentalPagingApi::class)
    private val pager: Flow<PagingData<CharacterModel>>
        get() = Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PAGE_PREFETCH_DISTANCE,
                enablePlaceholders = true
            ),
            remoteMediator = remoteMediator,
        ) {
            ModelsPagingSource(characterDao, ioDispatcher).apply {
                remoteMediator.invalidatePagingSourceCallback = { invalidate() }
            }
        }.flow.map { pagingData ->
            pagingData.map {
                when (it) {
                    is CharacterModel.CarouselItem -> CharacterModel.CarouselItem(it.itemName)
                    is CharacterModel.CharacterItem -> CharacterModel.CharacterItem(it.character)
                    is CharacterModel.TitleItem -> CharacterModel.TitleItem(it.title)
                    is CharacterModel.CharacterDtoItem -> CharacterModel.CharacterDtoItem(it.character)
                }
            }
        }

    override val carouselCharacters: Flow<PagingData<CharacterModel>>
        get() = pager.flowOn(ioDispatcher)
            .map {
                it.insertSeparators { before, _ ->
                    if (before is CharacterModel.CarouselItem) CharacterModel.TitleItem(
                        resources.getString(R.string.others_characters)
                    )
                    else null
                }
            }

    private val _upperCarouselCharacters: MutableStateFlow<List<CarouselCharacter>> =
        MutableStateFlow(listOf())
    override val upperCarouselCharacters: Flow<List<CarouselCharacter>>
        get() = _upperCarouselCharacters

    private val _character: MutableStateFlow<Character?> = MutableStateFlow(null)
    override val character: Flow<Character?>
        get() = _character

    override suspend fun getCharactersRandomly() {
        withContext(ioDispatcher) {
            val characters = characterDao.getCharactersRandomly().map { it.asCarouselCharacter() }
            _upperCarouselCharacters.emit(characters)
        }
    }

    override suspend fun fetchCharacter(characterId: Int) {
        withContext(ioDispatcher) {
            val marvelCharacter = characterDao.getCharacter(characterId).map { it?.asCharacter() }
            _character.emit(marvelCharacter.first())
        }
    }
}