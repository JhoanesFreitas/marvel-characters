package com.cajusoftware.marvelcharacters.data.database.sources

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.cajusoftware.marvelcharacters.data.database.dao.CharacterDao
import com.cajusoftware.marvelcharacters.data.domain.CharacterModel
import com.cajusoftware.marvelcharacters.data.network.responses.CharacterDataWrapperResponse
import com.cajusoftware.marvelcharacters.data.network.services.MarvelApiService
import com.cajusoftware.marvelcharacters.utils.asCharacterDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class ModelsPagingMediator(
    private val characterDao: CharacterDao,
    private val marvelApiService: MarvelApiService,
    private val ioDispatcher: CoroutineDispatcher
) : RemoteMediator<Int, CharacterModel>() {

    var invalidatePagingSourceCallback: (() -> Unit)? = null

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.SKIP_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CharacterModel>
    ): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> {
                    if (state.isEmpty()) 0
                    else (state.lastItemOrNull() as? CharacterModel.CharacterItem)?.character?.nextKey
                        ?: return MediatorResult.Success(
                            endOfPaginationReached = true
                        )
                }
                LoadType.APPEND -> {
                    val lastItem = if (state.isEmpty()) 0
                    else (state.lastItemOrNull() as? CharacterModel.CharacterItem)?.character?.nextKey
                        ?: return MediatorResult.Success(
                            endOfPaginationReached = true
                        )
                    lastItem
                }
            }


            val response: CharacterDataWrapperResponse =
                withContext(ioDispatcher) {
                    loadKey.let { marvelApiService.getCharacters(offset = it) }
                }

            response.data?.results?.map {
                it.asCharacterDto(response.copyright, response.data.nextKey)
            }?.let {
                withContext(ioDispatcher) {
                    characterDao.insertCharacters(it)
                    when {
                        loadKey == 0 && loadType == LoadType.PREPEND -> invalidatePagingSourceCallback?.invoke()
                        loadKey > 0 && loadType == LoadType.APPEND -> invalidatePagingSourceCallback?.invoke()
                        else -> {}
                    }
                }
            }

            MediatorResult.Success(
                endOfPaginationReached = response.data?.hasNext?.not() == true
            )

        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}