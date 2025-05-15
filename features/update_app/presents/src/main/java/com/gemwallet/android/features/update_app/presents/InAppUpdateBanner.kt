package com.gemwallet.android.features.update_app.presents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.update_app.viewmodels.InAppUpdateViewModels
import com.gemwallet.android.ui.R

@Composable
fun InAppUpdateBanner() {
    val viewModel: InAppUpdateViewModels = hiltViewModel()
    val context = LocalContext.current

    val updateAvailable by viewModel.updateAvailable.collectAsStateWithLifecycle()
//    val updateTask by viewModel.updateTask.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()

    val coroutine = rememberCoroutineScope()

    if (updateAvailable == null) {
        return
    }

    Row {
        Column {
            Text(
                if (progress == null) {
                    stringResource(R.string.update_app_title)
                } else {
                    "Updating"
                }
            )
            progress?.let { progress ->
                LinearProgressIndicator(
                    progress = { progress.toFloat() * 0.01f }
                )
            }
            Button(onClick = { viewModel.startDownload() } ) {
                Text("Start update")
            }
        }
    }
}