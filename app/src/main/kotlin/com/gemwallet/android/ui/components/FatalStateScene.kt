package com.gemwallet.android.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.designsystem.padding16
import com.gemwallet.android.ui.components.screen.Scene

@Composable
fun FatalStateScene(
    title: String,
    message: String,
    onCancel: (() -> Unit)?,
    onTryAgain: (() -> Unit)? = null,
) {
    Scene(
        title = title,
        padding = PaddingValues(padding16),
        onClose = onCancel,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    modifier = Modifier,
                    text = message,
                )
                if (onTryAgain != null) {
                    Spacer16()
                    Button(
                        modifier = Modifier,
                        onClick = onTryAgain
                    ) {
                        Text(text = stringResource(id = R.string.common_try_again))
                    }
                }
            }
        }
    }
}