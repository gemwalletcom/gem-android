package com.gemwallet.android.features.transactions.components

import android.icu.util.Calendar
import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.components.TransactionItem
import com.wallet.core.primitives.TransactionExtended
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.transactionsList(
    items: List<TransactionExtended>,
    onTransactionClick: (String) -> Unit
) {
    val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
    var prev = 0L
    val calendar = Calendar.getInstance()

    items.forEachIndexed { index, item ->
        calendar.timeInMillis = item.transaction.createdAt
        calendar[Calendar.MILLISECOND] = 999
        calendar[Calendar.SECOND] = 59
        calendar[Calendar.MINUTE] = 59
        calendar[Calendar.HOUR_OF_DAY] = 23
        val createdAt = calendar.time.time
        if (prev != createdAt) {
            stickyHeader {
                val title = if (DateUtils.isToday(createdAt) || DateUtils.isToday(createdAt + DateUtils.DAY_IN_MILLIS)) {
                    DateUtils.getRelativeTimeSpanString(item.transaction.createdAt, System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS).toString()
                } else {
                    dateFormat.format(Date(item.transaction.createdAt))
                }
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colorScheme.background)
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 0.dp),
                    text = title
                )
            }
        }
        item(key = item.transaction.id) {
            TransactionItem(item, index == items.size - 1, onTransactionClick)
        }
        prev = createdAt
    }
}