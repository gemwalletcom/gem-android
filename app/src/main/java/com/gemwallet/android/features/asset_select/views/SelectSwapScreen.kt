package com.gemwallet.android.features.asset_select.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.gemwallet.android.R
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.asset_select.viewmodels.AssetSelectViewModel
import com.gemwallet.android.features.swap.models.SwapPairSelect
import com.gemwallet.android.ui.components.getBalanceInfo
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.EVMChain

@Composable
fun SelectSwapScreen(
    select: SwapPairSelect,
    onCancel: () -> Unit,
    onSelect: ((SwapPairSelect) -> Unit)?,
    viewModel: AssetSelectViewModel = hiltViewModel()
) {
    val predicate: (AssetId) -> Boolean = remember(select.fromId?.toIdentifier(), select.toId?.toIdentifier()) {
        { other ->
            val chain = other.chain
            val isEVMChain = EVMChain.entries.map { it.string }.contains(chain.string)
            (isEVMChain || chain == Chain.Solana) && (
                other.toIdentifier() != select.oppositeId()?.toIdentifier()
                && other.toIdentifier() != select.change()?.toIdentifier()
                && (select.oppositeId() == null || select.oppositeId()?.chain == other.chain)
            )
        }
    }
    AssetSelectScreen(
        title = when (select) {
            is SwapPairSelect.From -> stringResource(id = R.string.swap_you_pay)
            is SwapPairSelect.To -> stringResource(id = R.string.swap_you_receive)
        },
        titleBadge = { null },
        itemTrailing = { getBalanceInfo(it)() },
        predicate = predicate,
        onSelect = { onSelect?.invoke(select.select(it)) },
        onCancel = onCancel,
        viewModel = viewModel,
    )
}