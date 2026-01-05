package com.gemwallet.android.ui.components.list_item.transaction

import android.icu.util.Calendar
import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.gemwallet.android.domains.transaction.aggregates.TransactionDataAggregate
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.list_item.property.itemsPositioned
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.transactionsList(
    items: List<TransactionDataAggregate>,
    onTransactionClick: (String) -> Unit
) {
    val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
    val calendar = Calendar.getInstance()

    items.groupBy { item ->
        calendar.timeInMillis = item.createdAt // TODO: Out to TransactionDataAggregator
        calendar[Calendar.MILLISECOND] = 999
        calendar[Calendar.SECOND] = 59
        calendar[Calendar.MINUTE] = 59
        calendar[Calendar.HOUR_OF_DAY] = 23
        val createdAt = calendar.time.time
        createdAt
    }.forEach { entry ->
        val createdAt = entry.key
        stickyHeader {
            val title = if (DateUtils.isToday(createdAt) || DateUtils.isToday(createdAt + DateUtils.DAY_IN_MILLIS)) {
                DateUtils.getRelativeTimeSpanString(createdAt, System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS).toString()
            } else {
                dateFormat.format(Date(createdAt))
            }
            SubheaderItem(
                title = title,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface),
            )
        }
        itemsPositioned(entry.value, key = {index, item -> item.id}) { position, item ->
            TransactionItem(
                data = item,
                listPosition = position,
                onClick = { onTransactionClick(item.id) }
            )
        }
    }
}