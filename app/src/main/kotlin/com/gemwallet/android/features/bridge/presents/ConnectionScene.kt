package com.gemwallet.android.features.bridge.presents

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.bridge.viewmodel.ConnectionViewModel
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.ListItem
import com.gemwallet.android.ui.components.list_item.ListItemSupportText
import com.gemwallet.android.ui.components.list_item.ListItemTitleText
import com.gemwallet.android.ui.components.screen.Scene
import java.text.DateFormat
import java.util.Date

@Composable
fun ConnectionScene(
    onCancel: () -> Unit,
    viewModel: ConnectionViewModel = hiltViewModel(),
) {
    val connection by viewModel.connection.collectAsStateWithLifecycle()

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
        LazyColumn {
            connection?.let {
                item { ConnectionItem(it) }
                item {
                    ListItem(
                        title = { ListItemTitleText(stringResource(id = R.string.common_wallet)) },
                        trailing = { ListItemSupportText(it.wallet.name) },
                    )
                }
                item {
                    ListItem(
                        title = { ListItemTitleText(stringResource(id = R.string.transaction_date)) },
                        trailing = {
                            val expire = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(it.session.expireAt))
                            ListItemSupportText(expire)
                        },
                    )
                }
            }
        }
    }
}