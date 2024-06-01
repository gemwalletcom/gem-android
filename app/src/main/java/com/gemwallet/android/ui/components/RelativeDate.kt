package com.gemwallet.android.ui.components

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
        (DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT) as SimpleDateFormat)
            .apply {
                if (createdAt.get(Calendar.YEAR) == Calendar.getInstance()
                        .get(Calendar.YEAR)
                ) {
                    applyPattern(
                        toPattern().replace(
                            "[^\\p{Alpha}\\W]*y+[^\\p{Alpha}\\W]*".toRegex(),
                            ""
                        )
                    )
                }
            }.format(createdAt.time)
    }
}