package com.gemwallet.android.features.import_wallet.views

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.blockchain.operators.walletcore.WCFindPhraseWord
import com.gemwallet.android.features.import_wallet.components.ImportInput
import com.gemwallet.android.features.import_wallet.components.WalletNameTextField
import com.gemwallet.android.features.import_wallet.components.WalletTypeTab
import com.gemwallet.android.features.import_wallet.viewmodels.ImportType
import com.gemwallet.android.features.import_wallet.viewmodels.ImportViewModel
import com.gemwallet.android.features.onboarding.AcceptTermsScreen
import com.gemwallet.android.interactors.ImportError
import com.gemwallet.android.ui.DisableScreenShooting
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.designsystem.Spacer4
import com.gemwallet.android.ui.components.designsystem.Spacer8
import com.gemwallet.android.ui.components.designsystem.padding16
import com.gemwallet.android.ui.components.designsystem.space4
import com.gemwallet.android.ui.components.parseMarkdownToAnnotatedString
import com.gemwallet.android.ui.components.screen.ModalBottomSheet
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.theme.WalletTheme
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.NameRecord
import com.wallet.core.primitives.WalletType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    importType: ImportType,
    onImported: () -> Unit,
    onCancel: () -> Unit
) {
    DisableScreenShooting()

    val viewModel: ImportViewModel = hiltViewModel()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        viewModel.importSelect(importType)

        onDispose {  }
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var isAcceptedTerms by remember {
        mutableStateOf(
            context.getSharedPreferences("terms", Context.MODE_PRIVATE).getBoolean("is_accepted", false)
        )
    }
    val state = rememberModalBottomSheetState(true)

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

    if (!isAcceptedTerms) {
        ModalBottomSheet(
            onDismissRequest = { onCancel() },
            sheetState = state,
        ) {
            AcceptTermsScreen(onCancel = onCancel) { isAcceptedTerms = true }
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
    val inputState = remember {
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
    val nameRecordState = remember(nameRecord?.address) { mutableStateOf(nameRecord) }
    var dataErrorState by remember(dataError) { mutableStateOf(dataError) }

    Scene(
        title = stringResource(id = R.string.wallet_import_title),
        padding = PaddingValues(padding16),
        onClose = onCancel,
        mainAction = {
            MainActionButton(
                title = stringResource(id = R.string.wallet_import_action),
                onClick = {
                    onImport(nameState, generatedName, inputState.value.text, nameRecordState.value)
                },
            )
        },
    ) {
        LazyColumn {
            item {
                WalletNameTextField(
                    value = nameState,
                    onValueChange = { newValue -> nameState = newValue },
                    placeholder = stringResource(id = R.string.wallet_name),
                    error = walletNameError,
                )
                Spacer16()
            }
            typeSelection(importType) {
                onTypeChange(it)
                inputState.value = TextFieldValue()
            }
            dataInput(importType, inputState, nameRecordState) {
                dataErrorState = null
            }
            errorMessage(dataErrorState)
        }
    }
}

private fun LazyListScope.dataInput(
    importType: ImportType,
    inputState: MutableState<TextFieldValue>,
    nameRecordState: MutableState<NameRecord?>,
    onChange: () -> Unit,
) {
    item {
        val suggestions = remember { mutableStateListOf<String>() }

        ImportInput(
            inputState = inputState.value,
            importType = importType,
            onValueChange = { query ->
                inputState.value = query
                suggestions.clear()

                onChange()

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
                val result = WCFindPhraseWord().invoke(word)
                suggestions.addAll(result)
            }
        ) {
            nameRecordState.value = it
        }
        if (importType.walletType == WalletType.view) {
            Spacer4()
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = parseMarkdownToAnnotatedString(
                    stringResource(R.string.wallet_import_address_warning)
                ),
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
        if (suggestions.isNotEmpty() && importType.walletType != WalletType.view) {
            Spacer8()
            LazyRow {
                items(suggestions) { word ->
                    SuggestionChip(
                        onClick = {
                            val processed = setSuggestion(inputState.value, word)
                            inputState.value = processed
                            suggestions.clear()
                            onChange()
                        },
                        label = { Text(text = word) }
                    )
                    Spacer8()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun LazyListScope.typeSelection(
    importType: ImportType,
    onTypeChange: (WalletType) -> Unit,
) {
    if (importType.walletType == WalletType.multicoin) {
        return
    }
    item {
        PrimaryTabRow(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
            selectedTabIndex = 0,
            indicator = { Box {} },
            containerColor = Color.Transparent,//(0xFFEBEBEB),
            divider = {}
        ) {
            WalletTypeTab(WalletType.single, importType.walletType, onTypeChange)
            WalletTypeTab(WalletType.private_key, importType.walletType, onTypeChange)
            WalletTypeTab(WalletType.view, importType.walletType, onTypeChange)
        }
        Spacer16()
    }
}

fun LazyListScope.errorMessage(error: ImportError?) {
    item {
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
            null -> return@item
        }
        Text(text = text, color = MaterialTheme.colorScheme.error)
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