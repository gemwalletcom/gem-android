package com.gemwallet.features.transfer_amount.presents.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.itemsPositioned
import com.gemwallet.android.ui.components.screen.ModalBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectLeverageDialog(
    leverages: List<Int>,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsPositioned(leverages) { position, item ->
                PropertyItem(
                    action = "${item}x",
                    listPosition = position,
                ) {
                    onSelect(item)
                    onDismiss()
                }
            }
        }
    }
}