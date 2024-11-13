package com.gemwallet.android.features.bridge.connection.views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.features.bridge.connection.viewmodels.ConnectionViewModel
import com.gemwallet.android.features.bridge.connections.views.ConnectionItem
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.Container
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.components.designsystem.Spacer16

@Composable
fun ConnectionScene(
    connectionId: String,
    onCancel: () -> Unit,
    viewModel: ConnectionViewModel = hiltViewModel(),
) {
    val state by viewModel.sceneState.collectAsStateWithLifecycle()

    DisposableEffect(connectionId) {
        viewModel.refresh(connectionId)

        onDispose {  }
    }
    Scene(
        title = stringResource(id = R.string.wallet_connect_title),
        backHandle = true,
        mainAction = {
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                onClick = { viewModel.disconnect(onCancel) },
            ) {
                Text(
                    modifier = Modifier.padding(4.dp),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    text = stringResource(id = R.string.wallet_connect_disconnect).uppercase(),
                )
            }
        },
        onClose = onCancel,
    ) {
        Container {
            ConnectionItem(state.connection)
        }
        Spacer16()
        Table(
            items = listOf(
                CellEntity(stringResource(id = R.string.common_wallet), state.walletName),
                CellEntity("Expire", state.connection.expire)
            ),
        )
    }
}