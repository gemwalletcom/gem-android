package com.gemwallet.android

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import com.gemwallet.android.ui.components.RootWarningDialog
import com.gemwallet.android.ui.components.isDeviceRooted
import com.gemwallet.android.ui.theme.WalletTheme

abstract class SecureBaseFragmentActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
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

