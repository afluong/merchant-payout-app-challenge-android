package com.example.androidinterview.feature.merchant.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.androidinterview.core.network.ApiService
import com.example.androidinterview.feature.merchant.data.mapper.toDomain
import com.example.androidinterview.feature.merchant.domain.model.ActivityItem

class ActivityPagingDataSource(
    private val apiService: ApiService
) : PagingSource<String, ActivityItem>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, ActivityItem> {
        return try {
            val cursor = params.key
            val response = apiService.getActivities(cursor = cursor, limit = params.loadSize)
            val domainItems = response.items.map { it.toDomain() }

            LoadResult.Page(
                data = domainItems,
                prevKey = null,
                nextKey = if (response.has_more) response.next_cursor else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, ActivityItem>): String? {
        return state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.prevKey
                ?: state.closestPageToPosition(position)?.nextKey
        }
    }
}