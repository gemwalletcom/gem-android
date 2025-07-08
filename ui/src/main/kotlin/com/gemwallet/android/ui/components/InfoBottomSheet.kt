package com.gemwallet.android.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.image.getIconUrl
import com.gemwallet.android.ui.components.screen.ModalBottomSheet
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState
import kotlinx.coroutines.launch
import uniffi.gemstone.Config
import uniffi.gemstone.DocsUrl

sealed class InfoSheetEntity(
    val icon: Any,
    val badgeIcon: Any? = null,
    @param:StringRes val title: Int,
    @param:StringRes val description: Int,
    val titleArgs: List<Any>? = null,
    val descriptionArgs: List<Any>? = null,
    val actionLabel: String? = null,
    val action: (() -> Unit)? = null,
    val infoUrl: String? = null,
) {
    class NetworkFeeInfo(networkTitle: String, networkSymbol: String) : InfoSheetEntity(
        icon = R.drawable.ic_network_fee,
        title = R.string.transfer_network_fee,
        description = R.string.info_network_fee_description,
        infoUrl = Config().getDocsUrl(DocsUrl.NETWORK_FEES),
        descriptionArgs = listOf(networkTitle, networkSymbol),
    )

    class NetworkBalanceRequiredInfo(chain: Chain, value: String, actionLabel: String, action: () -> Unit) : InfoSheetEntity(
        icon = chain.asset().getIconUrl(),
        title = R.string.info_insufficient_network_fee_balance_title,
        description = R.string.info_insufficient_network_fee_balance_description,
//        infoUrl = Config().getDocsUrl(DocsUrl.NETWORK_FEES),
        action = action,
        actionLabel = actionLabel,
        titleArgs = listOf(chain.asset().symbol),
        descriptionArgs = listOf(value, chain.asset().name, chain.asset().symbol),
    )

    class StakeLockTimeInfo(icon: Any) : InfoSheetEntity(
        icon = icon,
        badgeIcon = null,
        title = R.string.stake_lock_time,
        description = R.string.info_lock_time_description,
        infoUrl = Config().getDocsUrl(DocsUrl.STAKING_LOCK_TIME),
    )

    class TransactionInfo(icon: Any, state: TransactionState) : InfoSheetEntity(
        icon = icon,
        badgeIcon = when (state) {
            TransactionState.Pending -> R.drawable.transaction_state_pending
            TransactionState.Confirmed -> R.drawable.transaction_state_success
            TransactionState.Failed, TransactionState.Reverted -> R.drawable.transaction_state_error
        },
        title = when (state) {
            TransactionState.Pending -> R.string.transaction_status_pending
            TransactionState.Confirmed -> R.string.transaction_status_confirmed
            TransactionState.Failed -> R.string.transaction_status_failed
            TransactionState.Reverted -> R.string.transaction_status_reverted
        },
        description = when (state) {
            TransactionState.Pending -> R.string.info_transaction_pending_description
            TransactionState.Confirmed -> R.string.info_transaction_success_description
            TransactionState.Failed, TransactionState.Reverted -> R.string.info_transaction_error_description
        },
        infoUrl = Config().getDocsUrl(DocsUrl.TRANSACTION_STATUS),
    )

    object WatchWalletInfo : InfoSheetEntity(
        icon = R.drawable.ic_splash,
        badgeIcon = R.drawable.watch_badge,
        title = R.string.info_watch_wallet_title,
        description = R.string.info_watch_wallet_description,
        infoUrl = Config().getDocsUrl(DocsUrl.WHAT_IS_WATCH_WALLET),
    )

    object PriceImpactInfo : InfoSheetEntity(
        icon = R.drawable.ic_splash,
        title = R.string.info_price_impact_title,
        description = R.string.info_price_impact_description,
        infoUrl = Config().getDocsUrl(DocsUrl.PRICE_IMPACT),
    )

    object AssetStatusSuspiciousInfo : InfoSheetEntity(
        icon = R.drawable.ic_splash,
        title = R.string.asset_verification_suspicious,
        description = R.string.info_asset_status_suspicious_description,
        infoUrl = Config().getDocsUrl(DocsUrl.TOKEN_VERIFICATION),
    )

    object AssetStatusUnverifiedInfo : InfoSheetEntity(
        icon = R.drawable.ic_splash,
        title = R.string.asset_verification_unverified,
        description = R.string.info_asset_status_unverified_description,
        infoUrl = Config().getDocsUrl(DocsUrl.TOKEN_VERIFICATION),
    )
}

/// https://gist.github.com/binrebin/f3dad29956eb8dcb760a38ce86a9553b
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoBottomSheet(
    item: InfoSheetEntity?,
    onClose: (() -> Unit)
) {
    if (item == null) return
    val uriHandler = LocalUriHandler.current
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            scope.launch { sheetState.hide() }.invokeOnCompletion { onClose.invoke() }
        }
    ) {
        Column(
            modifier = Modifier .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer16()
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                AsyncImage(
                    modifier = Modifier.size(120.dp).clip(CircleShape),
                    model = item.icon,
                    contentDescription = ""
                )
                if (item.badgeIcon != null) {
                    AsyncImage(
                        modifier = Modifier
                            .size(48.dp)
                            .border(
                                border = BorderStroke(4.dp, color = MaterialTheme.colorScheme.background),
                                shape = CircleShape,
                            )
                            .clip(CircleShape),
                        model = item.badgeIcon,
                        contentDescription = ""
                    )
                }
            }
            Spacer16()
            Text(
                text = parseMarkdownToAnnotatedString(
                    markdown = item.titleArgs?.takeIf { it.isNotEmpty() }
                        ?.let { stringResource(item.title, *it.toTypedArray()) }
                        ?: stringResource(item.title)
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 32.dp),
                text = parseMarkdownToAnnotatedString(
                    markdown = item.descriptionArgs?.takeIf { it.isNotEmpty() }
                        ?.let { stringResource(item.description, *it.toTypedArray()) }
                        ?: stringResource(item.description)
                ),
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 32.dp),
            ) {
                MainActionButton(
                    title = item.actionLabel ?: stringResource(R.string.common_learn_more),
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion { onClose.invoke() }
                        item.action?.let { it() } ?: item.infoUrl?.let { uriHandler.openUri(it) }
                    },
                )
            }
        }
    }
}