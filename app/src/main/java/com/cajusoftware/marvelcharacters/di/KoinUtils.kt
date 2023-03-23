package com.cajusoftware.marvelcharacters.di

import android.content.Context
import com.cajusoftware.marvelcharacters.data.database.dao.CharacterDao
import com.cajusoftware.marvelcharacters.data.database.sources.ModelsPagingMediator
import com.cajusoftware.marvelcharacters.data.network.services.MarvelApiService
import com.cajusoftware.marvelcharacters.data.repositories.CharacterRepository
import com.cajusoftware.marvelcharacters.data.repositories.CharacterRepositoryImpl
import com.cajusoftware.marvelcharacters.ui.details.CharacterDetailViewModel
import com.cajusoftware.marvelcharacters.ui.home.CharacterViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit

fun provideMarvelService(retrofit: Retrofit): MarvelApiService =
    retrofit.create(MarvelApiService::class.java)

fun provideCharacterRepository(
    context: Context,
    characterDao: CharacterDao,
    remoteMediator: ModelsPagingMediator,
    ioDispatcher: CoroutineDispatcher
): CharacterRepository =
    CharacterRepositoryImpl(context.resources, characterDao, remoteMediator, ioDispatcher)

fun provideCharacterViewModel(characterRepository: CharacterRepository): CharacterViewModel =
    CharacterViewModel(characterRepository)

fun provideCharacterDetailViewModel(characterRepository: CharacterRepository): CharacterDetailViewModel =
    CharacterDetailViewModel(characterRepository)

fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

const val DEFAULT_DISPATCHER = "defaultDispatcher"
const val IO_DISPATCHER = "ioDispatcher"
const val MAIN_DISPATCHER = "mainDispatcher"
