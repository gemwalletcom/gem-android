package com.gemwallet.android.math

import android.text.format.DateUtils
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

fun getRelativeDate(timestamp: Long): String {
    if (timestamp == 0L) {
        return ""
    }
    return if (DateUtils.isToday(timestamp) || DateUtils.isToday(timestamp + DateUtils.DAY_IN_MILLIS)) {
        DateUtils.getRelativeTimeSpanString(
            timestamp,
            System.currentTimeMillis(),
            DateUtils.DAY_IN_MILLIS
        ).toString() +
                " " + DateFormat.getTimeInstance(DateFormat.SHORT)
            .format(Date(timestamp))
    } else {
        val createdAt = Calendar.getInstance()
        createdAt.timeInMillis = timestamp
        (DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT) as SimpleDateFormat)
            .format(createdAt.time)
    }
}