package com.gemwallet.android.ui.components.list_item

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.models.ListPosition

@Composable
fun SwitchProperty(
    text: String,
    checked: Boolean,
    listPosition: ListPosition = ListPosition.Single,
    onCheckedChange: (Boolean) -> Unit,
) {
    PropertyItem(
        title = { PropertyTitleText(text) },
        data = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        },
        listPosition = listPosition,
    )
}