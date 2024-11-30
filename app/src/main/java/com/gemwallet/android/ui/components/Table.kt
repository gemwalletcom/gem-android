package com.gemwallet.android.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.components.designsystem.Spacer4
import com.gemwallet.android.ui.components.designsystem.padding16
import com.gemwallet.android.ui.components.designsystem.padding8
import com.gemwallet.android.ui.components.designsystem.trailingIcon20
import com.gemwallet.android.ui.components.image.AsyncImage

data class CellEntity<T>(
    val label: T,
    val data: String,
    val dataColor: Color? = null,
    val support: String? = null,
    val actionIcon: (@Composable () -> Unit)? = null,
    val trailingIcon: String? = null,
    val icon: Any? = null,
    val trailing: (@Composable () -> Unit)? = null,
    val info: InfoSheetEntity? = null,
    val dropDownActions: (@Composable (() -> Unit) -> Unit)? = null,
    val showActionChevron: Boolean = true,
    val testTag: String = "",
    val action: (() -> Unit)? = null,
)

@Composable
fun Table(
    items: List<CellEntity<out Any>?>,
) {
    var isDropDownShow by remember { mutableStateOf<CellEntity<out Any>?>(null) }
    val items = items.mapNotNull { it }
    if (items.isEmpty()) {
        return
    }
    Container {
        Column {
            for (i in items.indices) {
                val item = items[i]
                Box {
                    Cell(
                        label = if (item.label is Int) {
                            stringResource(id = item.label)
                        } else {
                            item.label as String
                        },
                        icon = item.icon,
                        data = item.data,
                        dataColor = item.dataColor,
                        support = item.support,
                        action = item.action,
                        longAction = if (item.dropDownActions == null) null else {{ isDropDownShow = item }},
                        actionIcon = item.actionIcon,
                        showActionChevron = item.showActionChevron,
                        trailingIcon = item.trailingIcon,
                        trailing = item.trailing,
                        info = item.info,
                        testTag = item.testTag,
                    )
                    DropdownMenu(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        expanded = isDropDownShow == item && item.dropDownActions != null,
                        offset = DpOffset(padding16, padding8),
                        containerColor = MaterialTheme.colorScheme.background,
                        onDismissRequest = { isDropDownShow = null },
                    ) {
                        item.dropDownActions?.invoke {
                            isDropDownShow = null
                        }
                    }
                }
                if (i < items.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.4.dp)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Cell(
    testTag: String,
    label: @Composable () -> Unit,
    data: @Composable () -> Unit,
    support: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    trailingIcon: String? = null,
    action: (() -> Unit)? = null,
    showActionChevron: Boolean = true,
    longAction: (() -> Unit)? = null,
    actionIcon: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .testTag(testTag)
            .combinedClickable(
                enabled = action != null || longAction != null,
                onClick = { action?.invoke() },
                onLongClick = { longAction?.invoke() }
            )
            .fillMaxWidth()
            .padding(
                start = padding16,
                top = padding16,
                bottom = padding16,
                end = if (action == null) padding16 else padding8
            )
            ,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        label()
        Spacer(modifier = Modifier.width(20.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End,
        ) {
            data()
            support?.invoke()
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.size(8.dp))
            trailing()
        }
        if (!trailingIcon.isNullOrEmpty()) {
            Spacer(modifier = Modifier.size(8.dp))
            AsyncImage(trailingIcon, trailingIcon20)
        }
        if (action != null) {
            if (showActionChevron) {
                actionIcon?.invoke() ?:
                Icon(
                    painter = rememberVectorPainter(image = Icons.Default.ChevronRight),
                    contentDescription = "open_provider_select",
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

@Composable
private fun Cell(
    label: String,
    data: String,
    testTag: String,
    icon: Any? = null,
    dataColor: Color? = null,
    support: String? = null,
    actionIcon: (@Composable () -> Unit)? = null,
    showActionChevron: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
    trailingIcon: String? = null,
    info: InfoSheetEntity? = null,
    longAction: (() -> Unit)? = null,
    action: (() -> Unit)? = null,
) {
    Cell(
        label = {
            if (icon != null) {
                AsyncImage(icon, 24.dp)
                Spacer(modifier = Modifier.size(8.dp))
            }
            Text(
                text = label,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
            )
            info?.let { InfoButton(it) }
        },
        data = {
            MiddleEllipsisText(
                modifier = Modifier,
                text = data,
                textAlign = TextAlign.End,
                color = dataColor ?: MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        support = if (support != null) {
            {
                MiddleEllipsisText(
                    text = support,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            null
        },
        trailing = trailing,
        trailingIcon = trailingIcon,
        action = action,
        showActionChevron = showActionChevron,
        longAction = longAction,
        actionIcon = actionIcon,
        testTag = testTag,
    )
}

@Composable
private fun InfoButton(entity: InfoSheetEntity) {
    var showBottomSheet by remember { mutableStateOf(false) }
    Spacer4()
    Icon(
        modifier = Modifier
            .size(trailingIcon20)
            .clickable(onClick = { showBottomSheet = true }),
        imageVector = Icons.Outlined.Info,
        contentDescription = "",
        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
    )
    if (showBottomSheet) {
        InfoBottomSheet(entity) {
            showBottomSheet = false
        }
    }
}