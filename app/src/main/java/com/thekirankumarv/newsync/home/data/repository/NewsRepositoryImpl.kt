package com.thekirankumarv.newsync.home.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.thekirankumarv.newsync.home.data.remote.NewsApi
import com.thekirankumarv.newsync.home.data.remote.NewsPagingSource
import com.thekirankumarv.newsync.home.data.remote.SearchNewsPagingSource
import com.thekirankumarv.newsync.home.domain.model.Article
import com.thekirankumarv.newsync.home.domain.repository.NewsRepository

import kotlinx.coroutines.flow.Flow

class NewsRepositoryImpl(private val newsApi: NewsApi): NewsRepository {

    override fun getNews(sources: List<String>): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(pageSize = 10),
            pagingSourceFactory = {
                NewsPagingSource(newsApi = newsApi, sources = sources.joinToString(separator = ","))
            }
        ).flow
    }

    override fun searchNews(searchQuery: String, sources: List<String>): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(pageSize = 10),
            pagingSourceFactory = {
                SearchNewsPagingSource(
                    api = newsApi,
                    searchQuery = searchQuery,
                    sources = sources.joinToString(separator = ",")
                )
            }
        ).flow
    }

}