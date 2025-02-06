package com.thekirankumarv.newsync.home.data.remote.dto

import com.thekirankumarv.newsync.home.domain.model.Article

data class NewsResponse(
    val articles: List<Article>,
    val status: String,
    val totalResults: Int
)