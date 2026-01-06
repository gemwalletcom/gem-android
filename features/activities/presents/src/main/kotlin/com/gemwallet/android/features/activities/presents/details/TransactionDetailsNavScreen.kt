package com.gemwallet.android.features.activities.presents.details

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.gemwallet.android.features.activities.viewmodels.TransactionDetailsViewModel

@Composable
fun TransactionDetailsNavScreen(
    onCancel: () -> Unit,
    viewModel: TransactionDetailsViewModel = hiltViewModel(),
) {

}