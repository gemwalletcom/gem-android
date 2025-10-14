package com.gemwallet.features.transfer_amount.presents.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.theme.WalletTheme
import com.gemwallet.android.ui.theme.paddingDefault
import com.gemwallet.android.ui.theme.paddingHalfSmall
import com.wallet.core.primitives.Resource
import com.wallet.core.primitives.TransactionType

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun LazyListScope.resourceSelect(
    txType: TransactionType,
    selected: Resource,
    onSelect: (Resource) -> Unit
) {
    if (txType != TransactionType.StakeFreeze && txType != TransactionType.StakeUnfreeze) {
        return
    }
    item {
        Row(
            Modifier.padding(horizontal = paddingDefault),
            horizontalArrangement = Arrangement.spacedBy(paddingHalfSmall),
        ) {
            listOf(Resource.Bandwidth, Resource.Energy).forEachIndexed { index, item ->
                ToggleButton(
                    modifier = Modifier.semantics { role = Role.RadioButton },
                    checked = item == selected,
                    onCheckedChange = { onSelect(item) },
                    colors = ToggleButtonDefaults.toggleButtonColors().copy(containerColor = MaterialTheme.colorScheme.background),
                    shapes =
                        when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                .copy(checkedShape = ButtonGroupDefaults.connectedLeadingButtonShape)
                            Resource.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                .copy(checkedShape = ButtonGroupDefaults.connectedTrailingButtonShape)
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                .copy(checkedShape = ShapeDefaults.Small)
                        },
                ) {
                    Text(
                        stringResource(
                            when (item) {
                                Resource.Bandwidth -> R.string.stake_resource_bandwidth
                                Resource.Energy -> R.string.stake_resource_energy
                            }
                        ),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewFreezeVarian() {
    WalletTheme {
        LazyColumn {
            resourceSelect(TransactionType.StakeFreeze, selected = Resource.Energy) {}
        }
    }
}