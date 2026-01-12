package com.gemwallet.features.transfer_amount.presents.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.TabsBar
import com.gemwallet.android.ui.theme.WalletTheme
import com.wallet.core.primitives.Resource
import com.wallet.core.primitives.TransactionType

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun LazyListScope.resourceSelect(
    txType: TransactionType,
    selected: Resource,
    onSelect: (Resource) -> Unit
) {
    if (txType != TransactionType.StakeFreeze && txType != TransactionType.StakeUnfreeze) {
        return
    }
    item {
        TabsBar(listOf(Resource.Bandwidth, Resource.Energy), selected, onSelect) { item ->
            Text(
                stringResource(
                    when (item) {
                        Resource.Bandwidth -> R.string.stake_resource_bandwidth
                        Resource.Energy -> R.string.stake_resource_energy
                    }
                ),
            )
        }
    }
}

@Preview
@Composable
fun PreviewFreezeVarian() {
    WalletTheme {
        LazyColumn {
            resourceSelect(TransactionType.StakeFreeze, selected = Resource.Energy) {}
        }
    }
}