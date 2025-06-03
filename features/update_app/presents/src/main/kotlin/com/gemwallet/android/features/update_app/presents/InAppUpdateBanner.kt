package com.gemwallet.android.features.update_app.presents

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.update_app.viewmodels.DownloadState
import com.gemwallet.android.features.update_app.viewmodels.InAppUpdateViewModels
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.designsystem.Spacer4
import com.gemwallet.android.ui.components.designsystem.padding16
import com.gemwallet.android.ui.components.screen.ModalBottomSheet
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InAppUpdateBanner() {
    val viewModel: InAppUpdateViewModels = hiltViewModel()

    val updateAvailable by viewModel.updateAvailable.collectAsStateWithLifecycle()
    val state by viewModel.downloadState.collectAsStateWithLifecycle()
    var requestInstallPermissions = remember { mutableStateOf(false) }

    if (updateAvailable == null) {
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(padding16),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = if (state == DownloadState.Idle || state == DownloadState.Success) {
                    stringResource(R.string.update_app_title)
                } else {
                    "Downloading..."
                }
            )

            state.let { state ->
                when (state) {
                    DownloadState.Error,
                    DownloadState.Success,
                    DownloadState.Idle -> TextButton(
                        onClick = { requestInstallPermissions.value = !viewModel.update() }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("UPDATE")
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
    RequestInstallPermissions(requestInstallPermissions)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RequestInstallPermissions(state: MutableState<Boolean>) {
    if (!state.value) {
        return
    }
    val context = LocalContext.current
    ModalBottomSheet(
        onDismissRequest = { state.value = false },
    ) {
        Column(
            modifier = Modifier .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Allow installation",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 32.dp),
                text = "Go to settings and allow installation from external sources",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Row {
                Button(onClick = { state.value = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
                Spacer16()
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
                    Text("Go to settings")
                }
            }
        }
    }
}