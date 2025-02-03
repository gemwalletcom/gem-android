package com.gemwallet.android.features.stake.stake.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.gemwallet.android.ext.claimed
import com.gemwallet.android.features.assets.model.PriceUIState
import com.gemwallet.android.features.stake.components.DelegationItem
import com.gemwallet.android.features.stake.model.availableIn
import com.gemwallet.android.features.stake.stake.model.StakeError
import com.gemwallet.android.features.stake.stake.model.StakeUIState
import com.gemwallet.android.model.AmountParams
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.InfoSheetEntity
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.actions.AmountTransactionAction
import com.gemwallet.android.ui.theme.WalletTheme
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.StakeChain
import com.wallet.core.primitives.TransactionType
import com.wallet.core.primitives.WalletType

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StakeScene(
    uiState: StakeUIState.Loaded,
    amountAction: AmountTransactionAction,
    onRefresh: () -> Unit,
    onConfirm: () -> Unit,
    onDelegation: (String, String) -> Unit,
    onCancel: () -> Unit,
) {
    val pullRefreshState = rememberPullRefreshState(uiState.loading, { onRefresh() })

    Scene(
        title = stringResource(id = R.string.transfer_stake_title),
        onClose = onCancel,
    ) {
        Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    SubheaderItem(title = uiState.title)
                    Table(
                        items = listOf(
                            CellEntity(
                                stringResource(id = R.string.stake_apr, ""),
                                data = PriceUIState.formatPercentage(uiState.apr, false)
                            ),
                            CellEntity(
                                label = stringResource(id = R.string.stake_lock_time),
                                data = "${uiState.lockTime} days",
                                info = InfoSheetEntity.StakeLockTimeInfo(icon = uiState.assetIcon ?: "")
                            ),
                        ),
                    )
                }

                actions(
                    assetId = uiState.assetId,
                    stakeChain = uiState.stakeChain,
                    rewardsAmount = uiState.rewardsAmount,
                    hasRewards = uiState.hasRewards,
                    amountAction = amountAction,
                    onConfirm = onConfirm,
                    walletType = uiState.walletType,
                )

                items(uiState.delegations) {
                    DelegationItem(
                        assetDecimals = uiState.assetDecimals,
                        assetSymbol = uiState.assetSymbol,
                        delegation = it,
                        completedAt = availableIn(it),
                        onClick = { onDelegation(it.validator.id, it.base.delegationId) }
                    )
                }
            }
            PullRefreshIndicator(uiState.loading, pullRefreshState, Modifier.align(Alignment.TopCenter))
        }
    }
}

private fun LazyListScope.actions(
    assetId: AssetId,
    stakeChain: StakeChain,
    rewardsAmount: String,
    hasRewards: Boolean,
    amountAction: AmountTransactionAction,
    walletType: WalletType,
    onConfirm: () -> Unit
) {
    if (walletType == WalletType.view) {
        return
    }
    item {
        Spacer16()
        SubheaderItem(title = stringResource(R.string.common_manage))
        val cells = mutableListOf<CellEntity<Any>>(
            CellEntity(label = stringResource(id = R.string.wallet_stake), data = "") {
                amountAction(
                    AmountParams.buildStake(
                        assetId = assetId,
                        txType = TransactionType.StakeDelegate,
                    )
                )
            },
        )
        if (hasRewards && stakeChain.claimed()) {
            cells.add(
                CellEntity(
                    label = stringResource(id = R.string.transfer_claim_rewards_title),
                    data = rewardsAmount,
                    action = onConfirm
                )
            )
        }
        Table(items = cells)
        Spacer16()
    }
}

@Composable
@Preview
fun PreviewStakeScene() {
    WalletTheme {
        StakeScene(
            uiState = StakeUIState.Loaded(
                loading = false,
                error = StakeError.None,
                walletType = WalletType.single,
                assetId = AssetId(Chain.Cosmos),
                stakeChain = StakeChain.Cosmos,
                assetDecimals = 8,
                assetSymbol = "ATOM",
                ownerAddress = "",
                title = "Cosmos (ATOM)",
                apr = 13.94,
                lockTime = 21,
                hasRewards = true,
            ),
            onRefresh = { },
            amountAction = { _ -> },
            onConfirm = { },
            onDelegation = {_, _ -> },
        ) {

        }
    }
}