package com.gemwallet.features.asset.presents.details.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.theme.paddingLarge

@Composable
internal fun EmptyTransactionsItem(size: Int, modifier: Modifier = Modifier) {
    if (size > 0) {
        return
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = paddingLarge)
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(R.string.asset_state_empty_title)
        )
        // TODO: Add empty description
    }
}