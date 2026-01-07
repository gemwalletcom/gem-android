package com.gemwallet.android.features.activities.presents.details

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.activities.viewmodels.TransactionDetailsViewModel
import com.gemwallet.android.ui.components.screen.LoadingScene

@Composable
fun TransactionDetailsNavScreen(
    onCancel: () -> Unit,
    viewModel: TransactionDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.screenModel.collectAsStateWithLifecycle()
    val model = uiState
    var isShowFeeDetails by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val onShare = fun () {
        val type = "text/plain"
        val subject = uiState?.explorerUrl

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = type
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, subject)

        context.startActivity(Intent.createChooser(intent, uiState?.explorerName))
    }

    if (model == null) {
        LoadingScene(title = "", onCancel)
    } else {
        TransactionDetailsScene(
            model = model,
            onShare = onShare,
            onFeeDetails =  { isShowFeeDetails = true },
            onCancel = onCancel,
        )
    }
    if (isShowFeeDetails) {
        FeeDetailsDialog(model) { isShowFeeDetails = false }
    }
}