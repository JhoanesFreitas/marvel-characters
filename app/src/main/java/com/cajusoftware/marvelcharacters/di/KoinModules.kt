package com.cajusoftware.marvelcharacters.di

import androidx.room.Room
import com.cajusoftware.marvelcharacters.BuildConfig.BASE_URL
import com.cajusoftware.marvelcharacters.BuildConfig.DEBUG
import com.cajusoftware.marvelcharacters.data.database.MarvelDatabase
import com.cajusoftware.marvelcharacters.data.database.sources.ModelsPagingMediator
import com.cajusoftware.marvelcharacters.data.database.sources.ModelsPagingSource
import com.cajusoftware.marvelcharacters.data.domain.observers.CarouselCharacterSubject
import com.cajusoftware.marvelcharacters.data.network.interceptors.ConnectivityInterceptor
import com.cajusoftware.marvelcharacters.data.network.interceptors.NetworkInterceptor
import com.cajusoftware.marvelcharacters.ui.MainViewModel
import com.localebro.okhttpprofiler.OkHttpProfilerInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

val diModule = module {
    factory(named(DEFAULT_DISPATCHER)) { provideDefaultDispatcher() }
    factory(named(IO_DISPATCHER)) { provideIoDispatcher() }
    factory(named(MAIN_DISPATCHER)) { provideMainDispatcher() }
}

val dataModules = module {
    single {
        Room
            .databaseBuilder(get(), MarvelDatabase::class.java, "marvel_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    single { get<MarvelDatabase>().characterDao() }

    factory { ModelsPagingMediator(get(), get(), get(named(IO_DISPATCHER))) }

    factory { provideCharacterRepository(get(), get(), get(), get(named(IO_DISPATCHER))) }

    factory { ModelsPagingSource(get(), get(named(IO_DISPATCHER))) }

    single { CarouselCharacterSubject() }
}

val networkModules = module {
    factory {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    factory {
        OkHttpClient.Builder().apply {
            if (DEBUG) addInterceptor(OkHttpProfilerInterceptor())
            addInterceptor(NetworkInterceptor())
            addInterceptor(ConnectivityInterceptor(get()))
        }.build()
    }

    factory {
        provideMarvelService(get())
    }

    single {
        Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .baseUrl(BASE_URL)
            .client(get())
            .build()
    }
}

val uiModule = module {
    factory { provideCharacterViewModel(get()) }

    factory { provideCharacterDetailViewModel(get()) }

    factory { MainViewModel() }
}