package com.gemwallet.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.gemwallet.android.ui.theme.paddingDefault
import com.gemwallet.android.ui.theme.paddingHalfSmall

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> TabsBar(
    tabs: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    itemContent: @Composable RowScope.(T) -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = paddingDefault),
        horizontalArrangement = Arrangement.spacedBy(paddingHalfSmall),
    ) {
        tabs.forEachIndexed { index, item ->
            ToggleButton(
                modifier = Modifier.semantics { role = Role.RadioButton },
                checked = item == selected,
                onCheckedChange = { onSelect(item) },
                colors = ToggleButtonDefaults.toggleButtonColors()
                    .copy(containerColor = MaterialTheme.colorScheme.background),
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        .copy(checkedShape = ButtonGroupDefaults.connectedLeadingButtonShape)

                    tabs.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        .copy(checkedShape = ButtonGroupDefaults.connectedTrailingButtonShape)

                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        .copy(checkedShape = ShapeDefaults.Small)
                },
                content = { itemContent(item) }
            )
        }
    }
}