package com.gemwallet.features.swap.views.components

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.features.swap.viewmodels.models.SwapRate

@Composable
internal fun SwapRate(rate: SwapRate?) {
    var direction by remember { mutableStateOf(false) }

    rate ?: return

    PropertyItem(
        title = { PropertyTitleText(R.string.buy_rate) },
        data = {
            PropertyDataText(
                text = when (direction) {
                    true -> rate.reverse
                    false -> rate.forward
                },
                badge = {
                    Icon(
                        modifier = Modifier.Companion.clickable(onClick = {
                            direction = !direction
                        }),
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
            )
        }
    )
}