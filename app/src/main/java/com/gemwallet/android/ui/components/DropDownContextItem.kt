package com.gemwallet.android.ui.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
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
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
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
            .indication(interactionSource, LocalIndication.current)
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
                        val press = androidx.compose.foundation.interaction.PressInteraction.Press(it)
                        interactionSource.emit(press)
                        tryAwaitRelease()
                        interactionSource.emit(androidx.compose.foundation.interaction.PressInteraction.Release(press))
                    }
                )
        }
    ) {
        content()
        DropdownMenu(
            modifier = Modifier
                .onSizeChanged {
                    val height = with(density) { it.height.toDp() }
                    menuOffset = if (spaceToBottom - gesturePoint.y < height) {
                        gesturePoint.copy(y = gesturePoint.y - itemHeight - height)
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