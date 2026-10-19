package com.example.androidinterview.feature.merchant.presentation

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun formatDateHeader(date: Date): String {
    val calendar = Calendar.getInstance()
    val today = calendar.clone() as Calendar

    calendar.time = date

    val isToday = calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

    today.add(Calendar.DAY_OF_YEAR, -1)
    val isYesterday = calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

    return when {
        isToday -> "Today"
        isYesterday -> "Yesterday"
        else -> SimpleDateFormat("dd MMM yyyy", Locale.US).format(date)
    }
}