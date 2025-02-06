package com.thekirankumarv.newsync.home.presentation.search

import androidx.paging.PagingData
import com.thekirankumarv.newsync.home.domain.model.Article
import kotlinx.coroutines.flow.Flow

data class SearchState(
    val searchQuery: String = "",
    val articles: Flow<PagingData<Article>>? = null
)