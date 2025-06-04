package com.gemwallet.android.features.create_wallet.views

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.designsystem.Spacer8
import com.gemwallet.android.ui.components.designsystem.padding16
import com.gemwallet.android.ui.components.designsystem.padding4
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.actions.CancelAction
import com.gemwallet.android.ui.theme.WalletTheme

@Composable
fun PhraseAlertDialog(
    onAccept: () -> Unit,
    onCancel: CancelAction,
) {
    var isStoreChecked by remember { mutableStateOf(false) }
    var isShareChecked by remember { mutableStateOf(false) }
    var isRecoveryChecked by remember { mutableStateOf(false) }

    Scene(
        title = stringResource(R.string.wallet_new_title),
        mainAction = {
            MainActionButton(
                stringResource(R.string.common_continue),
                enabled = isStoreChecked && isShareChecked && isRecoveryChecked,
                onClick = onAccept,
            )
        },
        actions = {
            IconButton({}) {
                Icon(Icons.Outlined.Info, "")
            }
        },
        onClose = { onCancel() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding16),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.onboarding_security_create_wallet_intro_title),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.size(24.dp))
            InfoBlock(
                Icons.Default.Edit,
                R.string.onboarding_security_create_wallet_keep_safe_title,
                R.string.onboarding_security_create_wallet_keep_safe_subtitle,
                isStoreChecked,
            ) { isStoreChecked = it }
            Spacer(Modifier.size(24.dp))
            InfoBlock(
                Icons.Default.WarningAmber,
                R.string.secret_phrase_do_not_share_title,
                R.string.onboarding_security_create_wallet_do_not_share_subtitle,
                isShareChecked,
            ) { isShareChecked = it }
            Spacer(Modifier.size(24.dp))
            InfoBlock(
                Icons.Default.Diamond,
                R.string.onboarding_security_create_wallet_no_recovery_title,
                R.string.onboarding_security_create_wallet_no_recovery_subtitle,
                isRecoveryChecked
            ) { isRecoveryChecked = it }
        }
    }
}

@Composable
private fun InfoBlock(
    icon: ImageVector,
    @StringRes title: Int,
    @StringRes description: Int,
    isChecked: Boolean,
    onChecked: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(padding4)
    ) {
        Row(
            modifier = Modifier.padding(padding16),
        ) {
            Icon(imageVector = icon, "")
            Spacer8()
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(title),
                    style = MaterialTheme.typography.titleMedium.let {
                        it.copy(
                            lineHeightStyle = it.lineHeightStyle?.copy(
                                alignment = LineHeightStyle.Alignment.Top,
                            )
                        )
                    }
                )
                Text(
                    text = stringResource(description),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                Checkbox(isChecked, onChecked)
            }
        }
    }
}

@Preview
@Composable
fun PreviewPhraseAlertDialog() {
    WalletTheme {
        PhraseAlertDialog( {} ) {}
    }
}