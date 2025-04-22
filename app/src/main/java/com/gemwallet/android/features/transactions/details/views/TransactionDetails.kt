package com.gemwallet.android.features.transactions.details.views

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ext.asset
import com.gemwallet.android.features.transactions.details.viewmodels.TransactionDetailsViewModel
import com.gemwallet.android.features.transactions.details.viewmodels.getIcon
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.AmountListHead
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.InfoSheetEntity
import com.gemwallet.android.ui.components.LoadingScene
import com.gemwallet.android.ui.components.SwapListHead
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.components.clipboard.setPlainText
import com.gemwallet.android.ui.components.designsystem.trailingIconMedium
import com.gemwallet.android.ui.components.image.AsyncImage
import com.gemwallet.android.ui.components.image.getSupportIconUrl
import com.gemwallet.android.ui.components.image.getSwapProviderIcon
import com.gemwallet.android.ui.components.open
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator16
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.components.titles.getTransactionTitle
import com.gemwallet.android.ui.theme.pendingColor
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionType

@Composable
fun TransactionDetails(
    onCancel: () -> Unit,
    viewModel: TransactionDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.screenModel.collectAsStateWithLifecycle()

    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboard.current.nativeClipboard

    val model = uiState

    if (model == null) {
        LoadingScene(title = "", onCancel)
    } else {
        Scene(
            title = model.type.getTransactionTitle(model.direction, model.state, model.assetSymbol),
            onClose = onCancel,
        ) {
            when (model.type) {
                TransactionType.Swap -> SwapListHead(
                    fromAsset = model.fromAsset,
                    fromValue = model.fromValue!!,
                    toAsset = model.toAsset,
                    toValue = model.toValue!!,
                    currency = model.currency
                )
                else -> AmountListHead(
                    iconUrl = model.assetIcon,
                    supportIconUrl = model.assetId.getSupportIconUrl(),
                    placeholder = model.assetType.string,
                    amount = when (model.type) {
                        TransactionType.StakeDelegate,
                        TransactionType.StakeUndelegate,
                        TransactionType.StakeRewards,
                        TransactionType.StakeRedelegate,
                        TransactionType.StakeWithdraw,
                        TransactionType.Swap,
                        TransactionType.Transfer -> model.cryptoAmount
                        TransactionType.AssetActivation,
                        TransactionType.TokenApproval -> model.assetSymbol
                        TransactionType.TransferNFT -> TODO()
                        TransactionType.SmartContractCall -> TODO()
                    },
                    equivalent = when (model.type) {
                        TransactionType.StakeDelegate,
                        TransactionType.StakeUndelegate,
                        TransactionType.StakeRewards,
                        TransactionType.StakeRedelegate,
                        TransactionType.StakeWithdraw,
                        TransactionType.Swap,
                        TransactionType.Transfer -> model.fiatAmount
                        TransactionType.AssetActivation,
                        TransactionType.TokenApproval -> null
                        TransactionType.TransferNFT -> TODO()
                        TransactionType.SmartContractCall -> TODO()
                    },
                )
            }
            val cells = mutableListOf<CellEntity<Any>>()
            cells.add(
                CellEntity(
                    label = stringResource(id = R.string.transaction_date),
                    data = model.createdAt
                )
            )
            val dataColor = when (model.state) {
                TransactionState.Pending -> pendingColor
                TransactionState.Confirmed -> MaterialTheme.colorScheme.tertiary
                TransactionState.Failed,
                TransactionState.Reverted -> MaterialTheme.colorScheme.error
            }
            cells.add(
                CellEntity(
                    label = stringResource(id = R.string.transaction_status),
                    trailing = {
                        when (model.state) {
                            TransactionState.Pending -> CircularProgressIndicator16(color = pendingColor)
                            else -> null
                        }
                    },
                    data = when (model.state) {
                        TransactionState.Pending -> stringResource(id = R.string.transaction_status_pending)
                        TransactionState.Confirmed -> stringResource(id = R.string.transaction_status_confirmed)
                        TransactionState.Failed -> stringResource(id = R.string.transaction_status_failed)
                        TransactionState.Reverted -> stringResource(id = R.string.transaction_status_reverted)
                    },
                    dataColor = dataColor,
                    info = InfoSheetEntity.TransactionInfo(icon = model.assetIcon, state = model.state)
                ),
            )
            when (model.type) {
                TransactionType.Transfer -> when (model.direction) {
                    TransactionDirection.SelfTransfer,
                    TransactionDirection.Outgoing -> cells.add(
                        CellEntity(
                            label = stringResource(id = R.string.transaction_recipient),
                            data = model.to,
                            action = { clipboardManager.setPlainText(model.to) },
                            actionIcon = {
                                Icon(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = ""
                                )
                            }
                        )
                    )

                    TransactionDirection.Incoming -> cells.add(
                        CellEntity(
                            label = stringResource(id = R.string.transaction_sender),
                            data = model.from,
                            action = { clipboardManager.setPlainText(model.from) },
                            actionIcon = {
                                Icon(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = ""
                                )
                            }
                        )
                    )
                }
                TransactionType.Swap,
                TransactionType.TokenApproval,
                TransactionType.StakeDelegate,
                TransactionType.StakeUndelegate,
                TransactionType.StakeRewards,
                TransactionType.StakeRedelegate,
                TransactionType.AssetActivation,
                TransactionType.TransferNFT,
                TransactionType.SmartContractCall,
                TransactionType.StakeWithdraw -> {}
            }
            if (!model.memo.isNullOrEmpty()) {
                CellEntity(
                    label = stringResource(id = R.string.transfer_memo),
                    data = model.memo
                )
            }
            cells.add(
                CellEntity(
                    label = stringResource(id = R.string.transfer_network),
                    data = model.networkTitle,
                    trailing = {
                        AsyncImage(
                            model = model.assetId.chain.asset(),
                            size = trailingIconMedium,
                            placeholderText = model.assetType.string,
                        )
                    }
                )
            )
            model.provider?.let {
                cells.add(
                    CellEntity(
                        label = stringResource(id = R.string.swap_provider),
                        data = model.provider.name.lowercase(),
                        trailing = {
                            AsyncImage(
                                model = model.provider.getIcon(),
                                size = trailingIconMedium,
                                placeholderText = model.assetType.string,
                            )
                        }
                    )
                )
            }
            cells.add(
                CellEntity(
                    label = stringResource(id = R.string.transfer_network_fee),
                    data = model.feeCrypto,
                    support = model.feeFiat,
                    info = InfoSheetEntity.NetworkFeeInfo(networkTitle = model.networkTitle)
                )
            )
            cells.add(
                CellEntity(
                    label = stringResource(
                        id = R.string.transaction_view_on,
                        model.explorerName
                    ),
                    data = "",
                    action = { uriHandler.open(model.explorerUrl) }
                )
            )
            Table(items = cells)
        }
    }
}