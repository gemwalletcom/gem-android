package com.gemwallet.android.ui.components.list_item

import android.icu.util.Calendar
import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gemwallet.android.model.TransactionExtended
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.theme.paddingDefault
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.transactionsList(
    items: List<TransactionExtended>,
    onTransactionClick: (String) -> Unit
) {
    val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
    val calendar = Calendar.getInstance()
    items.groupBy { item ->
        calendar.timeInMillis = item.transaction.createdAt
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
            Text(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .fillMaxWidth()
                    .padding(start = paddingDefault, end = paddingDefault, top = 0.dp, bottom = 0.dp),
                text = title
            )
        }
        val size = entry.value.size
        itemsIndexed(entry.value, key = {index, item -> item.transaction.id}) { index, item ->
            TransactionItem(
                item,
                listPosition = ListPosition.getPosition(index, size),
                onTransactionClick
            )
        }
    }
}