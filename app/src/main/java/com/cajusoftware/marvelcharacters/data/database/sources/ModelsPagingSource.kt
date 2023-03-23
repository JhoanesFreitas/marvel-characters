package com.cajusoftware.marvelcharacters.data.database.sources

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.cajusoftware.marvelcharacters.BuildConfig
import com.cajusoftware.marvelcharacters.data.database.dao.CharacterDao
import com.cajusoftware.marvelcharacters.data.domain.CharacterModel
import com.cajusoftware.marvelcharacters.utils.asCarouselCharacter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

private const val STARTING_PAGING_INDEX = 0
private const val LASTING_INDEX_IN_FIRST_LOAD = 19

class ModelsPagingSource(
    private val characterDao: CharacterDao,
) : PagingSource<Int, CharacterModel>() {

    override fun getRefreshKey(state: PagingState<Int, CharacterModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CharacterModel> {
        //TODO injetar dispatcher
        val pageIndex = params.key ?: STARTING_PAGING_INDEX

        return try {

            val daoResult = withContext(Dispatchers.IO) {
                if (pageIndex == 0)
                    characterDao.getCharactersForPaging(pageIndex, LASTING_INDEX_IN_FIRST_LOAD)
                else
                    characterDao.getCharactersForPaging(pageIndex)
            }

            val result = mutableListOf<CharacterModel>()

            if (pageIndex == 0 && daoResult.isNotEmpty()) {
                result.add(CharacterModel.CarouselItem())
            }

            result.addAll(daoResult.map {
                CharacterModel.CharacterItem(it.asCarouselCharacter())
            })

            LoadResult.Page(
                data = result,
                prevKey = if (pageIndex > STARTING_PAGING_INDEX) (pageIndex - 1) else null,
                nextKey = if (daoResult.isNotEmpty() && pageIndex == 0) LASTING_INDEX_IN_FIRST_LOAD else if (daoResult.isNotEmpty()) pageIndex + BuildConfig.PAGE_SIZE else null
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        }
    }
}
