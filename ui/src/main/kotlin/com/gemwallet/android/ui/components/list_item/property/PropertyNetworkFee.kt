package com.gemwallet.android.ui.components.list_item.property

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.InfoSheetEntity
import com.gemwallet.android.ui.components.clickable
import com.gemwallet.android.ui.models.ListPosition

@Composable
fun PropertyNetworkFee(
    networkTitle: String,
    networkSymbol: String,
    feeCrypto: String,
    feeFiat: String,
    variantsAvailable: Boolean = false,
    showedCryptoAmount: Boolean = false,
    onSelectFee: (() -> Unit)? = null,
) {
    PropertyItem(
        modifier = Modifier.height(72.dp),
        title = {
            PropertyTitleText(R.string.transfer_network_fee, info = InfoSheetEntity.NetworkFeeInfo(networkTitle, networkSymbol))
        },
        data = {
            val dataModifier = if (variantsAvailable && onSelectFee != null) {
                Modifier.clickable(onClick = onSelectFee)
            } else {
                Modifier
            }
            Row(modifier = dataModifier, verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    if (showedCryptoAmount) {
                        Row(horizontalArrangement = Arrangement.End) { PropertyDataText(feeCrypto) }
                    }
                    Row(horizontalArrangement = Arrangement.End) { PropertyDataText(feeFiat) }
                }
                if (variantsAvailable) {
                    DataBadgeChevron()
                }
            }
        },
        listPosition = ListPosition.Single,
    )
}

