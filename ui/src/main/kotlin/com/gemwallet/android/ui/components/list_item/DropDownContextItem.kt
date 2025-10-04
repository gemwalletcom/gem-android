package com.gemwallet.android.ui.components.list_item

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun DropDownContextItem(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    imeCompensate: Boolean, // TODO: Compose bug relative with bottom bar.
    onDismiss: () -> Unit,
    content: @Composable (modifier: Modifier) -> Unit,
    menuItems: @Composable ColumnScope.() -> Unit,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
) {
    val density = LocalDensity.current
    var itemHeight by remember { mutableStateOf(0.dp) }
    var gesturePoint by remember { mutableStateOf(DpOffset(0.dp, 0.dp)) }
    var spaceToBottom by remember { mutableStateOf(0.dp) }
    var menuOffset by remember { mutableStateOf(DpOffset(0.dp, 0.dp)) }
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .onGloballyPositioned { coords ->
                spaceToBottom = with(density) {
                    ((coords.parentCoordinates?.boundsInParent()
                        ?: Rect.Zero).height - coords.positionInParent().y).toDp()
                }
            }
            .onSizeChanged {
                itemHeight = with(density) { it.height.toDp() }
            }
    ) {
        content(
            modifier.indication(interactionSource, LocalIndication.current)
                .pointerInput(true) {
                    detectTapGestures(
                        onLongPress = {
                            gesturePoint = DpOffset(it.x.toDp(), it.y.toDp())
                            onLongClick()
                        },
                        onTap = {
                            onClick()
                        },
                        onPress = {
                            val press = PressInteraction.Press(it)
                            interactionSource.emit(press)
                            tryAwaitRelease()
                            interactionSource.emit(PressInteraction.Release(press))
                        }
                    )
                }
        )
        DropdownMenu(
            modifier = Modifier
                .onSizeChanged {
                    val height = with(density) { it.height.toDp() }
                    menuOffset = if (spaceToBottom - gesturePoint.y < height) {
                        if (imeCompensate) gesturePoint else gesturePoint.copy(y = gesturePoint.y - itemHeight - height)
                    } else {
                        gesturePoint.copy(y = gesturePoint.y - itemHeight)
                    }
                },
            expanded = isExpanded,
            offset = menuOffset,
            containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = onDismiss,
            content = menuItems,
        )
    }
}