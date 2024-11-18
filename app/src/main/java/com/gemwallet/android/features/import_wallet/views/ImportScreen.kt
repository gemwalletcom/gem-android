package com.gemwallet.android.features.import_wallet.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.blockchain.operators.walletcore.WCFindPhraseWord
import com.gemwallet.android.features.import_wallet.components.ImportInput
import com.gemwallet.android.features.import_wallet.components.WalletNameTextField
import com.gemwallet.android.features.import_wallet.components.WalletTypeTab
import com.gemwallet.android.features.import_wallet.viewmodels.ImportType
import com.gemwallet.android.features.import_wallet.viewmodels.ImportViewModel
import com.gemwallet.android.interactors.ImportError
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.designsystem.Spacer8
import com.gemwallet.android.ui.components.designsystem.padding16
import com.gemwallet.android.ui.components.designsystem.space4
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.theme.WalletTheme
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.NameRecord
import com.wallet.core.primitives.WalletType

@Composable
fun ImportScreen(
    importType: ImportType,
    onImported: () -> Unit,
    onCancel: () -> Unit
) {
    val viewModel: ImportViewModel = hiltViewModel()

    DisposableEffect(Unit) {
        viewModel.importSelect(importType)

        onDispose {  }
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ImportScene(
        importType = uiState.importType,
        generatedNameIndex = uiState.generatedNameIndex,
        chainName = uiState.chainName,
        walletName = uiState.walletName,
        walletNameError = uiState.walletNameError,
        nameRecord = uiState.nameRecord,
        dataError = uiState.dataError,
        onImport = { name, generatedName, value, nameRecord ->
            viewModel.import(name, generatedName, value, nameRecord, onImported)
        },
        onTypeChange = viewModel::chainType,
        onCancel = onCancel,
    )
    if (uiState.loading) {
        Dialog(
            onDismissRequest = {  },
            DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Box(
                contentAlignment= Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportScene(
    importType: ImportType,
    generatedNameIndex: Int,
    chainName: String,
    walletName: String,
    nameRecord: NameRecord?,
    dataError: ImportError?,
    walletNameError: String,
    onImport: (name: String, generatedName: String, value: String, nameRecord: NameRecord?) -> Unit,
    onTypeChange: (WalletType) -> Unit,
    onCancel: () -> Unit
) {
    var inputState by remember {
        mutableStateOf(TextFieldValue())
    }
    val chainWalletName = stringResource(id = R.string.wallet_default_name_chain, chainName, generatedNameIndex)
    val mainWalletName = stringResource(id = R.string.wallet_default_name, generatedNameIndex)
    val generatedName = remember(key1 = importType.walletType, key2 = generatedNameIndex) {
        if (generatedNameIndex == 0 || importType.walletType == WalletType.multicoin) {
            mainWalletName
        } else {
            chainWalletName
        }
    }
    var nameState by remember(walletName + generatedNameIndex) {
        mutableStateOf(walletName.ifEmpty { generatedName })
    }
    var autoComplete by remember {
        mutableStateOf("")
    }
    var nameRecordState by remember(nameRecord?.address) {
        mutableStateOf(nameRecord)
    }
    val suggestions = remember {
        mutableStateListOf<String>()
    }
    var dataErrorState by remember(dataError) {
        mutableStateOf(dataError)
    }

    Scene(
        title = stringResource(id = R.string.wallet_import_title),
        padding = PaddingValues(padding16),
        mainActionPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 48.dp),
        onClose = onCancel,
        mainAction = {
            MainActionButton(
                title = stringResource(id = R.string.wallet_import_action),
                onClick = {
                    onImport(nameState, generatedName, inputState.text, nameRecordState)
                },
            )
        },
    ) {
        WalletNameTextField(
            value = nameState,
            onValueChange = { newValue -> nameState = newValue },
            placeholder = stringResource(id = R.string.wallet_name),
            error = walletNameError,
        )
        Spacer16()
        if (importType.walletType != WalletType.multicoin) {
            PrimaryTabRow(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                selectedTabIndex = 0,
                indicator = { Box {} },
                containerColor = Color.Transparent,//(0xFFEBEBEB),
                divider = {}
            ) {
                WalletTypeTab(WalletType.single, importType.walletType) {
                    onTypeChange(it)
                    inputState = TextFieldValue()
                }
                WalletTypeTab(WalletType.private_key, importType.walletType) {
                    onTypeChange(it)
                    inputState = TextFieldValue()
                }
                WalletTypeTab(WalletType.view, importType.walletType) {
                    onTypeChange(it)
                    inputState = TextFieldValue()
                }
            }
            Spacer16()
        }

        ImportInput(
            inputState = inputState,
            importType = importType,
            onValueChange = { query ->
                inputState = query
                suggestions.clear()
                dataErrorState = null

                if (suggestions.isNotEmpty() && importType.walletType != WalletType.view) {
                    return@ImportInput
                }

                val cursorPosition = query.selection.start
                if (query.text.isEmpty()) {
                    return@ImportInput
                }
                val word = query.text.substring(0..<cursorPosition).split(" ")
                    .lastOrNull()
                if (word.isNullOrEmpty()) {
                    return@ImportInput
                }
                val result = WCFindPhraseWord().invoke(word.toString())
                suggestions.addAll(result)
            }
        ) {
            nameRecordState = it
        }

        if (suggestions.isNotEmpty() && importType.walletType != WalletType.view) {
            Spacer8()
            LazyRow {
                items(suggestions) { word ->
                    SuggestionChip(
                        onClick = {
                            val processed = setSuggestion(inputState, word)
                            inputState = processed
                            autoComplete = ""
                            suggestions.clear()
                        },
                        label = { Text(text = word) }
                    )
                    Spacer8()
                }
            }
        }

        val error = dataErrorState
        if (error != null) {
            Spacer(modifier = Modifier.size(space4))
            val text = when (error) {
                is ImportError.CreateError -> stringResource(R.string.errors_create_wallet, error.message ?: "")
                is ImportError.InvalidWords -> stringResource(
                    R.string.errors_import_invalid_secret_phrase_word,
                    error.words.joinToString()
                )
                ImportError.InvalidationSecretPhrase -> stringResource(R.string.errors_import_invalid_secret_phrase)
                ImportError.InvalidAddress -> stringResource(R.string.errors_invalid_address_name)
                ImportError.InvalidationPrivateKey -> "Invalid private key"
            }
            Text(text = text, color = MaterialTheme.colorScheme.error)
        }
    }
}

private fun setSuggestion(inputState: TextFieldValue, word: String): TextFieldValue {
    val cursorPosition = inputState.selection.start
    val inputFull = inputState.text
    val rightInput =
        inputState.text.substring(0..<cursorPosition)
    val leftInput = inputState.text.substring(cursorPosition)
    val lastInput = rightInput.split(" ").lastOrNull() ?: ""
    val phrase = rightInput.removeSuffix(lastInput)
    return TextFieldValue(
        text = inputFull.replaceRange(0, inputFull.length, "$phrase$word $leftInput"),
        selection = TextRange("$phrase$word ".length)
    )
}

@Composable
@Preview(device = Devices.NEXUS_6)
@Preview(device = Devices.NEXUS_7)
@Preview(showBackground = true, device = Devices.NEXUS_7)
@Preview(showBackground = true, device = Devices.NEXUS_5)
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
fun PreviewImportAddress() {
    WalletTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            ImportScene(
                importType = ImportType(chain = Chain.Bitcoin, walletType = WalletType.view),
                generatedNameIndex = 1,
                chainName = "Ethereum",
                walletName = "Foo wallet name",
                walletNameError = "Foo wallet name error",
                nameRecord = null,
                dataError = null,
                onImport = {_, _, _, _ -> },
                onTypeChange = {},
                onCancel = {},
            )
        }
    }
}