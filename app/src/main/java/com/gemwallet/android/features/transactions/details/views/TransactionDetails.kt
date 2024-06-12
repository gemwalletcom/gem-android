package com.gemwallet.android.features.transactions.details.views

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.ext.type
import com.gemwallet.android.features.transactions.details.model.TxDetailsSceneState
import com.gemwallet.android.features.transactions.details.viewmodels.TransactionDetailsViewModel
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.ui.components.AmountListHead
import com.gemwallet.android.ui.components.AsyncImage
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.CircularProgressIndicator16
import com.gemwallet.android.ui.components.LoadingScene
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.components.SwapListHead
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.components.titles.getTransactionTitle
import com.gemwallet.android.ui.theme.pendingColor
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionType

@Composable
fun TransactionDetails(
    txId: String,
    onCancel: () -> Unit,
    viewModel: TransactionDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(txId) {
        viewModel.setTxId(txId)

        onDispose {  }
    }

    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current

    when (uiState) {
        TxDetailsSceneState.Loading -> LoadingScene(title = "", onCancel)
        is TxDetailsSceneState.Loaded -> {
            val state: TxDetailsSceneState.Loaded = uiState as TxDetailsSceneState.Loaded
            Scene(
                title = state.type.getTransactionTitle(state.assetSymbol),
                onClose = onCancel,
            ) {
                when (state.type) {
                    TransactionType.Swap -> SwapListHead(
                        fromAsset = state.fromAsset!!,
                        fromValue = state.fromValue!!,
                        toAsset = state.toAsset!!,
                        toValue = state.toValue!!,
                    )
                    else -> AmountListHead(
                        iconUrl = state.assetIcon,
                        supportIconUrl = if (state.assetId.type() == AssetSubtype.NATIVE) null else state.assetId.chain.getIconUrl(),
                        placeholder = state.assetType.string,
                        amount = when (state.type) {
                            TransactionType.StakeDelegate,
                            TransactionType.StakeUndelegate,
                            TransactionType.StakeRewards,
                            TransactionType.StakeRedelegate,
                            TransactionType.StakeWithdraw,
                            TransactionType.Transfer -> state.cryptoAmount
                            TransactionType.Swap -> state.cryptoAmount
                            TransactionType.TokenApproval -> state.assetSymbol
                        },
                        equivalent = when (state.type) {
                            TransactionType.StakeDelegate,
                            TransactionType.StakeUndelegate,
                            TransactionType.StakeRewards,
                            TransactionType.StakeRedelegate,
                            TransactionType.StakeWithdraw,
                            TransactionType.Transfer -> state.fiatAmount
                            TransactionType.Swap -> state.fiatAmount
                            TransactionType.TokenApproval -> null
                        },
                    )
                }
                val cells = mutableListOf<CellEntity<Any>>()
                cells.add(
                    CellEntity(
                        label = stringResource(id = R.string.transaction_date),
                        data = state.createdAt
                    )
                )
                val dataColor = when (state.state) {
                    TransactionState.Pending -> pendingColor
                    TransactionState.Confirmed -> MaterialTheme.colorScheme.tertiary
                    TransactionState.Failed -> MaterialTheme.colorScheme.error
                    TransactionState.Reverted -> MaterialTheme.colorScheme.secondary
                }
                cells.add(
                    CellEntity(
                        label = stringResource(id = R.string.transaction_status),
                        trailing = {
                            when (state.state) {
                                TransactionState.Pending -> CircularProgressIndicator16(color = pendingColor)
                                TransactionState.Confirmed -> Icon(
                                    modifier = Modifier.size(16.dp),
                                    imageVector = Icons.Default.Done,
                                    contentDescription = "",
                                    tint = dataColor,
                                )
                                TransactionState.Failed -> Icon(
                                    modifier = Modifier.size(16.dp),
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "",
                                    tint = dataColor,
                                )
                                TransactionState.Reverted -> Icon(
                                    modifier = Modifier.size(16.dp),
                                    imageVector = Icons.AutoMirrored.Default.Undo,
                                    contentDescription = "",
                                    tint = dataColor,
                                )
                                else -> {}
                            }
                        },
                        data = when (state.state) {
                            TransactionState.Pending -> stringResource(id = R.string.transaction_status_pending)
                            TransactionState.Confirmed -> stringResource(id = R.string.transaction_status_confirmed)
                            TransactionState.Failed -> stringResource(id = R.string.transaction_status_failed)
                            TransactionState.Reverted -> stringResource(id = R.string.transaction_status_reverted)
                        },
                        dataColor = dataColor
                    ),
                )
                when (state.type) {
                    TransactionType.Transfer -> when (state.direction) {
                        TransactionDirection.SelfTransfer,
                        TransactionDirection.Outgoing -> cells.add(
                            CellEntity(
                                label = stringResource(id = R.string.transaction_recipient),
                                data = state.to,
                                action = { clipboardManager.setText(AnnotatedString(state.to)) },
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
                                data = state.from,
                                action = { clipboardManager.setText(AnnotatedString(state.from)) },
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
                    TransactionType.StakeWithdraw -> {
                    }
                }
                if (!state.memo.isNullOrEmpty()) {
                    CellEntity(
                        label = stringResource(id = R.string.transfer_memo),
                        data = state.memo
                    )
                }
                cells.add(
                    CellEntity(
                        label = stringResource(id = R.string.transfer_network),
                        data = state.networkTitle,
                        trailing = {
                            AsyncImage(
                                model = "file:///android_asset/chains/icons/${state.assetId.chain.string}.png",
                                contentDescription = "asset_icon",
                                placeholderText = state.assetType.string,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    )
                )
                cells.add(
                    CellEntity(
                        label = stringResource(id = R.string.transfer_network_fee),
                        data = state.feeCrypto,
                        support = state.feeFiat,
                    )
                )
                cells.add(
                    CellEntity(
                        label = stringResource(
                            id = R.string.transaction_view_on,
                            state.explorerName
                        ),
                        data = "",
                        action = { uriHandler.openUri(state.explorerUrl) }
                    )
                )
                Table(items = cells)
            }
        }
    }
}