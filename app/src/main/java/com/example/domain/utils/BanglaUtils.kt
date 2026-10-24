package com.example.domain.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BanglaUtils {
    private val englishToBanglaDigits = mapOf(
        '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
        '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯'
    )

    private val banglaToEnglishDigits = mapOf(
        '০' to '0', '১' to '1', '২' to '2', '৩' to '3', '৪' to '4',
        '৫' to '5', '৬' to '6', '৭' to '7', '৮' to '8', '৯' to '9'
    )

    fun toBanglaDigits(number: Long): String {
        return toBanglaDigits(number.toString())
    }

    fun toBanglaDigits(text: String): String {
        return text.map { englishToBanglaDigits[it] ?: it }.joinToString("")
    }

    fun toEnglishDigits(text: String): String {
        return text.map { banglaToEnglishDigits[it] ?: it }.joinToString("")
    }

    fun formatTaka(amount: Long, isBangla: Boolean = true, showSymbol: Boolean = true): String {
        val absAmount = kotlin.math.abs(amount)
        val formatted = String.format(Locale.US, "%,d", absAmount)
        val text = if (isBangla) toBanglaDigits(formatted) else formatted
        val symbol = if (showSymbol) "৳" else ""
        return if (amount < 0) "-$symbol$text" else "$symbol$text"
    }

    fun formatDate(timestamp: Long, isBangla: Boolean = true): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val dateStr = sdf.format(Date(timestamp))
        return if (isBangla) toBanglaDigits(dateStr) else dateStr
    }

    fun formatTime(timestamp: Long, isBangla: Boolean = true): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.US)
        val timeStr = sdf.format(Date(timestamp))
        return if (isBangla) toBanglaDigits(timeStr) else timeStr
    }

    fun formatDateTime(timestamp: Long, isBangla: Boolean = true): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
        val dtStr = sdf.format(Date(timestamp))
        return if (isBangla) toBanglaDigits(dtStr) else dtStr
    }
}
