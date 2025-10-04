package com.gemwallet.android.ui.components.list_item.property

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.models.getListPosition

inline fun <T> LazyListScope.itemsPositioned(
    items: List<T>,
    noinline key: ((index: Int, item: T) -> Any)? = null,
    crossinline contentType: (index: Int, item: T) -> Any? = { _, _ -> null },
    crossinline itemContent: @Composable LazyItemScope.(position: ListPosition, item: T) -> Unit,
) {
    itemsIndexed(items, key, contentType) { index, item ->
        val position = items.getListPosition(index)
        itemContent(position, item)
    }
}