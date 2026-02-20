package com.gemwallet.android.features.wallet.presents.components

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.property.DataBadgeChevron
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.models.ListPosition
import com.wallet.core.primitives.WalletType

@Composable
internal fun ShowSecretDataProperty(
    walletId: String,
    walletType: WalletType,
    onClick: (String) -> Unit,
) {
    val secretDataLabel = when (walletType) {
        WalletType.Multicoin,
        WalletType.Single -> stringResource(id = R.string.common_secret_phrase)
        WalletType.PrivateKey -> stringResource(R.string.common_private_key)
        WalletType.View -> return
    }
    PropertyItem(
        modifier = Modifier.clickable { onClick(walletId) },
        title = {
            PropertyTitleText(stringResource(R.string.common_show, secretDataLabel))
        },
        data = { PropertyDataText("", badge = { DataBadgeChevron() }) },
        listPosition = ListPosition.Single,
    )
}