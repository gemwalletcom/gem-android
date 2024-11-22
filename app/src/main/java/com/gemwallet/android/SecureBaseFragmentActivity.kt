package com.gemwallet.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.fragment.app.FragmentActivity
import com.gemwallet.android.ui.components.RootWarningDialog
import com.gemwallet.android.ui.components.isDeviceRooted
import com.gemwallet.android.ui.theme.WalletTheme

abstract class SecureBaseFragmentActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BaseContent()
            MainContent()
        }
    }

    @Composable
    protected abstract fun MainContent()

    @Composable
    private fun BaseContent() {
        WalletTheme {
            var showRootWarningDialog by remember { mutableStateOf(isDeviceRooted()) }

            if (showRootWarningDialog) {
                RootWarningDialog(
                    onCancel = { this.finishAffinity() },
                    onIgnore = { showRootWarningDialog = false }
                )
            }
        }
    }
}