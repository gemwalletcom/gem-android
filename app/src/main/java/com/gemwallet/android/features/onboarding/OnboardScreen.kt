package com.gemwallet.android.features.onboarding

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gemwallet.android.R
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.WalletTheme

@Composable
fun OnboardScreen(
    onCreateWallet: () -> Unit,
    onImportWallet: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 48.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Image(
                painterResource(id = R.drawable.brandmark),
                contentDescription = "welcome_icon",
                modifier = Modifier.size(100.dp),
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.welcome_title),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.displaySmall,
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            OnboardAction(text = R.string.wallet_create_new_wallet, testTag = "create", onClick = onCreateWallet)
            OnboardAction(text = R.string.wallet_import_existing_wallet, testTag = "import", onClick = onImportWallet)
        }
    }
}

@Composable
private fun OnboardAction(
    @StringRes text: Int,
    testTag: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Text(text = stringResource(id = text))
    }
    Spacer16()
}

@Preview
@Composable
fun PreviewWelcomeScreen() {
    WalletTheme {
        OnboardScreen(onCreateWallet = { }) {

        }
    }
}