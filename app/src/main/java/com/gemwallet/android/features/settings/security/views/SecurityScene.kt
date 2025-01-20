package com.gemwallet.android.features.settings.security.views

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.AuthRequest
import com.gemwallet.android.MainActivity
import com.gemwallet.android.features.settings.security.viewmodels.SecurityViewModel
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.components.screen.Scene

@Composable
fun SecurityScene(
    onCancel: () -> Unit,
    viewModel: SecurityViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var authRequired by remember {
        mutableStateOf(viewModel.authRequired())
    }
    val hideBalances by viewModel.isHideBalances.collectAsStateWithLifecycle()
    Scene(
        title = stringResource(id = (R.string.settings_security)),
        onClose = onCancel,
    ) {
        Table(
            items = listOf(
                CellEntity(
                    label = stringResource(id = R.string.settings_enable_passcode),
                    data = "",
                    trailing = {
                        Switch(
                            checked = authRequired,
                            onCheckedChange = {
                                MainActivity.requestAuth(context, AuthRequest.Enable) {
                                    viewModel.setAuthRequired(it)
                                    authRequired = it
                                }
                            }
                        )
                    }
                ),
                CellEntity(
                    label = stringResource(id = R.string.settings_hide_balance),
                    data = "",
                    trailing = {
                        Switch(
                            checked = hideBalances,
                            onCheckedChange = {
                                viewModel.setHideBalances()
                            }
                        )
                    }
                )
            )
        )
    }
}