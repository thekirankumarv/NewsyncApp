package com.thekirankumarv.newsync.home.domain.usecase.news

import androidx.paging.PagingData
import com.thekirankumarv.newsync.home.domain.model.Article
import com.thekirankumarv.newsync.home.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow

class SearchNews(
    private val newsRepository: NewsRepository
) {
    operator fun invoke(searchQuery: String, sources: List<String>): Flow<PagingData<Article>> {
        return newsRepository.searchNews(
            searchQuery = searchQuery,
            sources = sources
        )
    }
}