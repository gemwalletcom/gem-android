package com.gemwallet.android.features.wallet.presents

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
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType

@Composable
internal fun ShowSecretData(
    wallet: Wallet,
    onAuthRequest: (() -> Unit) -> Unit,
    onPhraseShow: () -> Unit,
) {
    if (wallet.type == WalletType.view) {
        return
    }
    PropertyItem(
        modifier = Modifier.Companion.clickable { onAuthRequest(onPhraseShow) },
        title = {
            PropertyTitleText(
                text = stringResource(
                    id = R.string.common_show,
                    if (wallet.type == WalletType.private_key)
                        stringResource(R.string.common_private_key)
                    else
                        stringResource(id = R.string.common_secret_phrase)
                )
            )
        },
        data = { PropertyDataText("", badge = { DataBadgeChevron() }) },
        listPosition = ListPosition.Single,
    )
}