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
    val data by viewModel.data.collectAsStateWithLifecycle()
    val model = data
    var isShowFeeDetails by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val onShare = fun () {
        val type = "text/plain"
        val subject = model?.explorer?.url

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = type
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, subject)

        context.startActivity(Intent.createChooser(intent, model?.explorer?.name))
    }

    if (model == null) {
        LoadingScene(title = "", onCancel)
    } else {
        TransactionDetailsScene(
            data = model,
            onShare = onShare,
            onFeeDetails =  { isShowFeeDetails = true },
            onCancel = onCancel,
        )
    }
    if (isShowFeeDetails) {
        FeeDetailsDialog(model?.fee) { isShowFeeDetails = false }
    }
}