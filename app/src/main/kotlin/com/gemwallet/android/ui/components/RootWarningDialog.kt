package com.gemwallet.android.ui.components

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.BuildConfig
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.open
import com.gemwallet.android.ui.theme.Spacer8
import uniffi.gemstone.Config
import uniffi.gemstone.DocsUrl
import java.io.File

@Composable
fun RootWarningDialog(onCancel: () -> Unit, onIgnore: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    AlertDialog(
        onDismissRequest = onIgnore,
        title = { Text(text = stringResource(R.string.rootcheck_security_alert)) },
        text = {
            Column {
                Text(text = stringResource(R.string.rootcheck_body))
                Spacer8()
                Text(
                    modifier = Modifier.clickable {
                        uriHandler.open(context, Config().getDocsUrl(DocsUrl.RootedDevice))
                    },
                    text = stringResource(R.string.common_learn_more),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(onClick = onCancel) {
                Text(text = stringResource(R.string.rootcheck_exit))
            }
        },
        dismissButton = {
            Button(onClick = onIgnore) {
                Text(text = stringResource(R.string.rootcheck_ignore))
            }
        }
    )
}

fun isDeviceRooted() = !BuildConfig.DEBUG && RootChecker().isDeviceRooted()

private class RootChecker {

    fun isDeviceRooted(): Boolean {
        return hasRootedFiles() || hasRootedProcesses() || hasTestKeys()
    }

    private fun hasRootedFiles(): Boolean {
        return arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        ).any { path -> File(path).exists() }
    }

    private fun hasRootedProcesses(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            process.inputStream.bufferedReader().use { it.readLine() != null }
        } catch (_: Exception) {
            false
        }
    }

    private fun hasTestKeys(): Boolean {
        return Build.TAGS?.contains("test-keys") == true
    }
}