package com.gemwallet.android.features.update_app.presents

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.update_app.viewmodels.DownloadState
import com.gemwallet.android.features.update_app.viewmodels.InAppUpdateViewModels
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.designsystem.Spacer4
import com.gemwallet.android.ui.components.designsystem.padding16
import com.gemwallet.android.ui.components.list_item.DropDownContextItem
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InAppUpdateBanner() {
    val viewModel: InAppUpdateViewModels = hiltViewModel()

    val updateAvailable by viewModel.updateAvailable.collectAsStateWithLifecycle()
    val state by viewModel.downloadState.collectAsStateWithLifecycle()

    var requestInstallPermissions = remember { mutableStateOf(false) }
    var isShowContextMenu by remember { mutableStateOf(false) }

    if (updateAvailable == null) {
        return
    }

    val action = remember {
        fun() {
            when (state) {
                DownloadState.Error,
                DownloadState.Success,
                DownloadState.Canceled,
                DownloadState.Idle -> requestInstallPermissions.value = !viewModel.update()

                DownloadState.Preparing,
                is DownloadState.Progress -> viewModel.cancel()
            }
        }
    }

    DropDownContextItem(
        isExpanded = isShowContextMenu,
        imeCompensate = false,
        onDismiss = { isShowContextMenu = false },
        content = {
            UpdateInfo(
                state,
                updateAvailable,
                action,
            )
        },
        menuItems = {
            when (state) {
                DownloadState.Idle,
                DownloadState.Error,
                DownloadState.Canceled,
                DownloadState.Success -> {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = R.string.update_app_action)) },
                        onClick = {
                            isShowContextMenu = false
                            requestInstallPermissions.value = !viewModel.update()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.common_skip)) },
                        onClick = {
                            isShowContextMenu = false
                            viewModel.skip()
                        },
                    )
                }
                DownloadState.Preparing,
                is DownloadState.Progress -> {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = R.string.common_cancel)) },
                        onClick = {
                            isShowContextMenu = false
                            viewModel.cancel()
                        },
                    )
                }
            }
        },
        onLongClick = { isShowContextMenu = true },
        onClick = action,
    )
    RequestInstallPermissions(requestInstallPermissions)
}

@Composable
private fun UpdateInfo(
    state: DownloadState,
    updateAvailable: String?,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(padding16),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(R.string.update_app_title)
                )
                Text(
                    modifier = Modifier.weight(1f),
                    text = updateAvailable ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }

            state.let { state ->
                when (state) {
                    DownloadState.Error,
                    DownloadState.Success,
                    DownloadState.Canceled,
                    DownloadState.Idle -> TextButton(
                        onClick = onAction,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.update_app_action))
                            Spacer4()
                            Icon(Icons.Default.ArrowCircleDown, "Update application", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    DownloadState.Preparing -> CircularProgressIndicator(
                        modifier = Modifier.size(size = 36.dp),
                        strokeWidth = 2.dp,
                    )
                    is DownloadState.Progress -> Box {
                        CircularProgressIndicator(
                            modifier = Modifier.size(size = 36.dp),
                            strokeWidth = 2.dp,
                            progress = { state.value },
                        )
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = "${(state.value * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }
        if (state == DownloadState.Error) {
            Text(
                text = stringResource(R.string.errors_error_occured),
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RequestInstallPermissions(state: MutableState<Boolean>) {
    if (!state.value) {
        return
    }
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = { state.value = false },
        containerColor = MaterialTheme.colorScheme.background,
        confirmButton = {
            Button(
                onClick = {
                    state.value = false
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = "package:${context.packageName}".toUri()
                        addFlags(FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            ) {
                Text(stringResource(R.string.update_app_permission_open_settings))
            }
        },
        dismissButton = {
            Button(onClick = { state.value = false }) {
                Text(stringResource(R.string.common_cancel))
            }
        },
        title = {
            Text(text = stringResource(R.string.update_app_permission_title))
        },
        text = {
            Text(text = stringResource(R.string.update_app_permission_description))
        }
    )
}