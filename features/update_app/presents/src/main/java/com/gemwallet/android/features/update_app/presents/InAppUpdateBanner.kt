package com.gemwallet.android.features.update_app.presents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.update_app.viewmodels.AppVersionDownloadStatus
import com.gemwallet.android.features.update_app.viewmodels.DownloadState
import com.gemwallet.android.features.update_app.viewmodels.InAppUpdateViewModels
import com.gemwallet.android.ui.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun InAppUpdateBanner(
    viewModel: InAppUpdateViewModels = hiltViewModel()
) {
    val context = LocalContext.current

    val updateAvailable by viewModel.updateAvailable.collectAsStateWithLifecycle()
    val updateTask by viewModel.updateTask.collectAsStateWithLifecycle()
    var progress by remember { mutableStateOf<AppVersionDownloadStatus?>(null) }

    val coroutine = rememberCoroutineScope()

    if (updateAvailable == null) {
        return
    }

    Row {
        Column {
            Text(
                if (updateTask == null) {
                    stringResource(R.string.update_app_title)
                } else {
                    "Updating"
                }
            )
            updateTask?.let {
                LinearProgressIndicator()
            }
        }
    }

    LaunchedEffect(updateTask) {
        coroutine.launch(Dispatchers.IO) {
            updateTask?.let {
                while (progress?.state != DownloadState.PROGRESS) {
                    progress = viewModel.observeDownload(it, context)
                    delay(60 * 1000)
                }
            }
        }
    }
}