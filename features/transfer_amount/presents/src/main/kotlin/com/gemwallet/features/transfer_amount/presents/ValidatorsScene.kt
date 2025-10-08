package com.gemwallet.features.transfer_amount.presents

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.list_item.ValidatorItem
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.theme.WalletTheme
import com.gemwallet.features.transfer_amount.models.ValidatorsUIState
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.DelegationValidator

@Composable
fun ValidatorsScene(
    uiState: ValidatorsUIState.Loaded,
    selectedValidatorId: String,
    onSelect: (String) -> Unit,
    onCancel: () -> Unit,
) {
    Scene(
        title = stringResource(id = R.string.stake_validators),
        onClose = onCancel,
    ) {
        LazyColumn {
            if (uiState.recomended.isNotEmpty()) {
                item {
                    SubheaderItem(title = stringResource(id = R.string.common_recommended))
                }
                val recommendedSize = uiState.recomended.size
                itemsIndexed(uiState.recomended, key = { index, item -> "recomended-${item.id}" }) { index, item ->
                    ValidatorItem(
                        data = item,
                        listPosition = ListPosition.getPosition(index, recommendedSize),
                        isSelected = selectedValidatorId == item.id,
                        onClick = onSelect
                    )
                }

            }
            item {
                if (uiState.recomended.isNotEmpty()) {
                    Spacer(modifier = Modifier.size(8.dp))
                }
                SubheaderItem(title = stringResource(id = R.string.stake_active))
            }
            val validatorsSize = uiState.validators.size
            itemsIndexed(uiState.validators, key = { index, item -> item.id }) { index, item ->
                ValidatorItem(
                    data = item,
                    listPosition = ListPosition.getPosition(index, validatorsSize),
                    isSelected = selectedValidatorId == item.id,
                    onClick = onSelect
                )
            }
        }
    }
}

@Composable
@Preview
fun PreviewValidatorsScene() {
    WalletTheme {
        ValidatorsScene(
            uiState = ValidatorsUIState.Loaded(
                recomended = emptyList(),
                validators = listOf(
                    DelegationValidator(
                        chain = Chain.Sei,
                        id = "some_validator_id",
                        name = "Castlenode",
                        isActive = true,
                        commission = 0.5,
                        apr = 9.10,
                    ),
                    DelegationValidator(
                        chain = Chain.Sei,
                        id = "some_validator_id_1",
                        name = "Ubik Capital 0%Fee",
                        isActive = true,
                        commission = 0.5,
                        apr = 10.000,
                    ),
                    DelegationValidator(
                        chain = Chain.Sei,
                        id = "some_validator_id_2",
                        name = "Virtual Hive",
                        isActive = true,
                        commission = 0.5,
                        apr = 9.50,
                    ),
                ),
            ),
            selectedValidatorId = "some_validator_id_1",
            onCancel = {},
            onSelect = {},
        )
    }
}