package com.gemwallet.features.banner.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.listItem
import com.gemwallet.android.ui.theme.listItemIconSize
import com.gemwallet.android.ui.theme.paddingDefault
import com.gemwallet.android.ui.theme.paddingSmall
import com.gemwallet.android.ui.theme.trailingIconSmall

@Composable
fun WelcomeBanner(
    isHidden: Boolean,
    isSynced: Boolean,
    cryptoTotal: Double,
    onBuy: () -> Unit,
    onReceive: () -> Unit,
    onClose: () -> Unit,
) {
    if (isSynced || cryptoTotal != 0.0 || isHidden) {
        return
    }
    Box(
        modifier = Modifier.fillMaxWidth().listItem().padding(paddingDefault),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(paddingSmall),
        ) {
            Icon(
                modifier = Modifier.size(listItemIconSize),
                imageVector = Icons.Default.CurrencyBitcoin,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.banner_onboarding_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.banner_onboarding_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(paddingDefault),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onBuy,
                ) {
                    Text(stringResource(R.string.wallet_buy))
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onReceive,
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    )
                ) {
                    Text(stringResource(R.string.wallet_receive))
                }
            }
        }
        Icon(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .clickable(onClick = onClose)
                .size(trailingIconSmall)
            ,
            imageVector = Icons.Default.Close,
            contentDescription = "",
            tint = MaterialTheme.colorScheme.secondary
        )
    }
}