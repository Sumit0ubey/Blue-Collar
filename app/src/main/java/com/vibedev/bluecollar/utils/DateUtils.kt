package com.vibedev.bluecollar.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun isThisWeek(date: Date): Boolean {
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance()
    cal1.time = date
    cal2.time = Date()
    return cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR) &&
            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
}

fun isToday(dateStr: String): Boolean {
    val sdfISO = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val date = try {
        sdfISO.parse(dateStr)
    } catch (e: Exception) {
        return false
    } ?: return false
    val cal1 = Calendar.getInstance()
    cal1.time = date
    val cal2 = Calendar.getInstance() // now
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun getTodayDateRangeISO(): Pair<String, String> {
    val sdfISO = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val startOfDay = cal.time

    cal.add(Calendar.DATE, 1)
    cal.add(Calendar.MILLISECOND, -1)
    val endOfDay = cal.time

    return Pair(sdfISO.format(startOfDay), sdfISO.format(endOfDay))
}

fun getThisWeekDateRangeISO(): Pair<String, String> {
    val sdfISO = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.firstDayOfWeek = Calendar.MONDAY
    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val startOfWeek = cal.time

    cal.add(Calendar.WEEK_OF_YEAR, 1)
    cal.add(Calendar.MILLISECOND, -1)
    val endOfWeek = cal.time

    return Pair(sdfISO.format(startOfWeek), sdfISO.format(endOfWeek))
}