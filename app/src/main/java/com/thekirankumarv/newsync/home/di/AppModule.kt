package com.thekirankumarv.newsync.home.di

import android.app.Application
import com.thekirankumarv.newsync.home.data.manager.LocalUserMangerImpl
import com.thekirankumarv.newsync.home.data.remote.NewsApi
import com.thekirankumarv.newsync.home.data.repository.NewsRepositoryImpl
import com.thekirankumarv.newsync.home.domain.LocalUserManger
import com.thekirankumarv.newsync.home.domain.repository.NewsRepository
import com.thekirankumarv.newsync.home.domain.usecase.app_entry.AppEntryUseCases
import com.thekirankumarv.newsync.home.domain.usecase.app_entry.ReadAppEntry
import com.thekirankumarv.newsync.home.domain.usecase.app_entry.SaveAppEntry
import com.thekirankumarv.newsync.home.domain.usecase.news.GetNews
import com.thekirankumarv.newsync.home.domain.usecase.news.NewsUseCases
import com.thekirankumarv.newsync.home.domain.usecase.news.SearchNews
import com.thekirankumarv.newsync.home.util.Constants.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLocalUserManger(
        application: Application
    ): LocalUserManger = LocalUserMangerImpl(context = application)

    @Provides
    @Singleton
    fun provideAppEntryUseCases(
        localUserManger: LocalUserManger
    ): AppEntryUseCases = AppEntryUseCases(
        readAppEntry = ReadAppEntry(localUserManger),
        saveAppEntry = SaveAppEntry(localUserManger)
    )

    @Provides
    @Singleton
    fun provideApiInstance(): NewsApi {
        return Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNewsRepository(
        newsApi: NewsApi
    ): NewsRepository {
        return NewsRepositoryImpl(newsApi)
    }

    @Provides
    @Singleton
    fun provideNewsUseCases(
        newsRepository: NewsRepository
    ): NewsUseCases {
        return NewsUseCases(
            getNews = GetNews(newsRepository),
            searchNews = SearchNews(newsRepository)
        )
    }

}