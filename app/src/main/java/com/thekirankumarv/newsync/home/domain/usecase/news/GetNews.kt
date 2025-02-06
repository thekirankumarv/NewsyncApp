package com.thekirankumarv.newsync.home.domain.usecase.news

import androidx.paging.PagingData
import com.thekirankumarv.newsync.home.domain.model.Article
import com.thekirankumarv.newsync.home.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow

class   GetNews(
    private val newsRepository: NewsRepository
) {
    operator fun invoke(sources: List<String>): Flow<PagingData<Article>> {
        return newsRepository.getNews(sources = sources)
    }
}