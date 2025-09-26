@file:OptIn(ExperimentalMaterial3Api::class)

package com.gemwallet.features.stake.presents.views

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.domains.asset.lockTime
import com.gemwallet.android.domains.asset.stakeChain
import com.gemwallet.android.domains.asset.title
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.format
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.InfoSheetEntity
import com.gemwallet.android.domains.asset.getIconUrl
import com.gemwallet.android.ui.components.list_item.DelegationItem
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.list_item.availableIn
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.PriceUIState
import com.gemwallet.android.ui.models.actions.AmountTransactionAction
import com.gemwallet.features.stake.presents.views.components.stakeActions
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.WalletType
import java.math.BigInteger

@Composable
fun StakeScene(
    inSync: Boolean,
    walletType: WalletType,
    assetInfo: AssetInfo,
    rewardsAmount: BigInteger,
    delegations: List<Delegation>,
    amountAction: AmountTransactionAction,
    onRefresh: () -> Unit,
    onConfirm: () -> Unit,
    onDelegation: (String, String) -> Unit,
    onCancel: () -> Unit,
) {
    val pullToRefreshState = rememberPullToRefreshState()

    Scene(
        title = stringResource(id = R.string.transfer_stake_title),
        onClose = onCancel,
    ) {
        PullToRefreshBox(
            modifier = Modifier,
            isRefreshing = inSync,
            onRefresh = onRefresh,
            state = pullToRefreshState,
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = inSync,
                    state = pullToRefreshState,
                    containerColor = MaterialTheme.colorScheme.background
                )
            }
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    SubheaderItem(title = assetInfo.title)
                }
                apr(assetInfo.stakeApr ?: 0.0)
                assetInfo.lockTime?.let { lockTime(it, assetInfo.id()) }

                stakeActions(
                    assetId = assetInfo.id(),
                    stakeChain = assetInfo.stakeChain ?: return@LazyColumn,
                    rewardsAmount = assetInfo.asset.format(Crypto(rewardsAmount)),
                    hasRewards = rewardsAmount > BigInteger.ZERO,
                    amountAction = amountAction,
                    onConfirm = onConfirm,
                    walletType = walletType,
                )

                items(delegations) {
                    DelegationItem(
                        asset = assetInfo.asset,
                        delegation = it,
                        completedAt = availableIn(it),
                        onClick = { onDelegation(it.validator.id, it.base.delegationId) }
                    )
                }
            }
        }
    }
}

private fun LazyListScope.lockTime(lockTime: Int, id: AssetId) {
    item {
        PropertyItem(
            title = stringResource(id = R.string.stake_lock_time),
            data = "$lockTime days",
            info = InfoSheetEntity.StakeLockTimeInfo(icon = id.getIconUrl())
        )
    }
}

internal fun LazyListScope.apr(apr: Double) {
    item {
        PropertyItem(
            title = stringResource(id = R.string.stake_apr, ""),
            data = PriceUIState.formatPercentage(apr, false) // TODO: Out to AssetInfo ext
        )
    }
}