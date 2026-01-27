package com.gemwallet.features.settings.settings.presents.views

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.LinkItem
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.features.settings.currency.presents.components.emojiFlags
import com.gemwallet.features.settings.settings.viewmodels.SettingsViewModel
import java.util.Locale

@Composable
fun PreferencesScene(
    onCurrencies: () -> Unit,
    onNetworks: () -> Unit,
    onPerpetual: () -> Unit,
    onCancel: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    Scene(
        title = stringResource(id = (R.string.settings_preferences_title)),
        onClose = onCancel,
    ) {
        LazyColumn {
            item {
                LinkItem(
                    title = stringResource(R.string.settings_currency),
                    icon = R.drawable.settings_currency,
                    listPosition = ListPosition.First,
                    supportingContent = {
                        Text(text = "${emojiFlags[uiState.currency.string]}  ${uiState.currency.string}")
                    },
                    onClick = onCurrencies,
                )
            }

            item {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    LinkItem(
                        title = stringResource(id = R.string.settings_language),
                        icon = R.drawable.settings_language,
                        supportingContent = {
                            val language = context.resources.configuration.getLocales()
                                .get(0).displayLanguage.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                            }
                            Text(text = language)
                        },
                        onClick = {
                            val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS)
                            intent.data = Uri.fromParts("package", context.packageName, null)
                            context.startActivity(intent)
                        }
                    )
                }
            }

            item {
                LinkItem(
                    title = stringResource(id = R.string.settings_networks_title),
                    icon = R.drawable.settings_networks,
                    listPosition = ListPosition.Last
                ) {
                    onNetworks()
                }
            }
        }
    }
}