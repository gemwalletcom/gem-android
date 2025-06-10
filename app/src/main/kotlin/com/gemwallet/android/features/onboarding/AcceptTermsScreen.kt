package com.gemwallet.android.features.onboarding

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.designsystem.Spacer8
import com.gemwallet.android.ui.components.designsystem.padding16
import com.gemwallet.android.ui.components.designsystem.padding4
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.actions.CancelAction
import com.gemwallet.android.ui.theme.WalletTheme

@Composable
fun AcceptTermsScreen(
    onCancel: CancelAction,
    onAccept: () -> Unit,
) {
    val context = LocalContext.current
    var isUnderstand1 by remember { mutableStateOf(false) }
    var isUnderstand2 by remember { mutableStateOf(false) }
    var isUnderstand3 by remember { mutableStateOf(false) }

    Scene(
        title = stringResource(R.string.onboarding_accept_terms_title),
        onClose = { onCancel() },
        mainAction = {
            MainActionButton(
                title = stringResource(R.string.onboarding_accept_terms_continue),
                enabled = isUnderstand1 && isUnderstand2 && isUnderstand3,
                onClick = {
                    context.getSharedPreferences("terms", Context.MODE_PRIVATE)
                        .edit {
                            putBoolean("is_accepted", true)
                        }
                    onAccept()
                }
            )
        }
    ) {
        LazyColumn (
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding16),
        ) {
            item {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.onboarding_accept_terms_message),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.size(24.dp))
            }
            termItem(
                isUnderstand1,
                R.string.onboarding_accept_terms_item1_message,
            ) { isUnderstand1 = !isUnderstand1 }
            termItem(
                isUnderstand2,
                R.string.onboarding_accept_terms_item2_message,
            ) { isUnderstand2 = !isUnderstand2 }
            termItem(
                isUnderstand3,
                R.string.onboarding_accept_terms_item3_message,
            ) { isUnderstand3 = !isUnderstand3 }
        }
    }
}

private fun LazyListScope.termItem(
    isUnderstand: Boolean,
    @StringRes description: Int,
    onClick: () -> Unit,
) {
    item {
        Card(
            modifier = Modifier.clickable(onClick = onClick),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(padding4)
        ) {
            Row(
                modifier = Modifier.padding(padding16),
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircleOutline,
                    contentDescription = "Accept term",
                    tint = if (isUnderstand) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                )
                Spacer8()
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(description),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
        Spacer(Modifier.size(24.dp))
    }
}

@Preview
@Composable
fun AcceptTermsScreenPreview() {
    WalletTheme {
        AcceptTermsScreen({}) { }
    }
}