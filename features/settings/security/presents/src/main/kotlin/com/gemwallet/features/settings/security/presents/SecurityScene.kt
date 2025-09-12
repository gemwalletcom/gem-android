package com.gemwallet.features.settings.security.presents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.model.AuthRequest
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.requestAuth
import com.gemwallet.android.ui.theme.Spacer4
import com.gemwallet.features.settings.security.viewmodels.SecurityViewModel

@Composable
fun SecurityScene(
    onCancel: () -> Unit,
    viewModel: SecurityViewModel = hiltViewModel(),
) {
    var authRequired by remember { mutableStateOf(viewModel.authRequired()) }
    val hideBalances by viewModel.isHideBalances.collectAsStateWithLifecycle()
    val lockInterval by viewModel.lockInterval.collectAsStateWithLifecycle()

    Scene(
        title = stringResource(id = (R.string.settings_security)),
        onClose = onCancel,
    ) {
        LazyColumn {
            enablePasscode(authRequired) {
                viewModel.setAuthRequired(it)
                authRequired = it
            }
            if (authRequired) {
                requiredAuthDelay(lockInterval, viewModel::setLockInterval)
            }
            hideBalanceItem(hideBalances, viewModel::setHideBalances)
        }
    }
}

private fun LazyListScope.enablePasscode(
    authRequired: Boolean,
    onAuthRequired: (Boolean) -> Unit,
) {
    item {
        val context = LocalContext.current
        PropertyItem(
            title = { PropertyTitleText(R.string.settings_enable_passcode) },
            data = {
                Switch(
                    authRequired,
                    onCheckedChange = {
                        context.requestAuth(AuthRequest.Enable) {
                            onAuthRequired(it)
                        }
                    }
                )
            },
        )
    }
}

private fun LazyListScope.requiredAuthDelay(
    currentInterval: Int,
    onSelect: (Int) -> Unit,
) {
    val locks = mapOf(
        0 to R.string.lock_immediately,
        1 to R.string.lock_one_minute,
        5 to R.string.lock_five_minutes,
        15 to R.string.lock_fifteen_minutes,
        60 to R.string.lock_one_hour,
        6 * 60 to R.string.lock_six_hours,
    )
    item {
        var isShowLockDelays by remember { mutableStateOf(false) }
        PropertyItem(
            title = { PropertyTitleText(R.string.lock_require_authentication) },
            data = {
                Box {
                    Row(
                        modifier = Modifier.clickable(onClick = { isShowLockDelays = true }),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(locks[currentInterval]!!),
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.MiddleEllipsis,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Icon(imageVector = Icons.Default.UnfoldMore, contentDescription = "Show lock timeouts")
                    }
                    DropdownMenu(
                        expanded = isShowLockDelays,
                        onDismissRequest = { isShowLockDelays = false },
                        containerColor = MaterialTheme.colorScheme.background,
                    ) {
                        for (interval in locks.keys) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        interval.takeIf { it == currentInterval }?.let {
                                            Icon(Icons.Default.Check, "", modifier = Modifier.size(20.dp))
                                        } ?: Spacer(modifier = Modifier.size(20.dp))
                                        Spacer4()
                                        Text(stringResource(locks[interval]!!))
                                    }
                                },
                                {
                                    onSelect(interval)
                                    isShowLockDelays = false
                                },
                            )
                        }
                    }
                }
            },
        )
    }
}

private fun LazyListScope.hideBalanceItem(
    hideBalances: Boolean,
    onHide: () -> Unit,
) {
    item {
        PropertyItem(
            title = { PropertyTitleText(R.string.settings_hide_balance) },
            data = {
                Switch(
                    checked = hideBalances,
                    onCheckedChange = { onHide() }
                )
            },
        )
    }
}