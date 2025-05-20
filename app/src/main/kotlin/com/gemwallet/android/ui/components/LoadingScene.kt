package com.gemwallet.android.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gemwallet.android.ui.components.designsystem.padding16
import com.gemwallet.android.ui.components.screen.Scene

@Composable
fun LoadingScene(title: String, onCancel: () -> Unit) {
    Scene(
        title = title,
        padding = PaddingValues(padding16),
        onClose = onCancel
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}