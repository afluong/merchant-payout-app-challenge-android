package com.example.androidinterview.core.network.model

data class PaginatedActivityResponse(
    val items: List<ActivityItemResponse>,
    val next_cursor: String?,
    val has_more: Boolean,
)