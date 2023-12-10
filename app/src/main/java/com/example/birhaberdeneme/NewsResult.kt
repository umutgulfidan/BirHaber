package com.example.birhaberdeneme

data class NewsResult(
    val status: String,
    val totalResults: Long,
    val articles: List<Article>,
)

data class Article(
    val source: Source,
    val author: String?,
    val title: String,
    val description: String,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String,
    val content: String,
)

data class Source(
    val id: String?,
    val name: String,)
