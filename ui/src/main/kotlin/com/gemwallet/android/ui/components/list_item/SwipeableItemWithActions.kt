package com.gemwallet.android.ui.components.list_item

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import com.gemwallet.android.ui.models.ListPosition
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// https://github.com/philipplackner/ComposeSwipeToReveal/blob/master/app/src/main/java/com/plcoding/composeswipetoreveal/SwipeableItemWithActions.kt
@Composable
fun SwipeableItemWithActions(
    isRevealed: Boolean,
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    listPosition: ListPosition = ListPosition.Middle,
    onExpanded: () -> Unit = {},
    onCollapsed: () -> Unit = {},
    content: @Composable (ListPosition) -> Unit
) {

    var contextMenuWidth by remember { mutableFloatStateOf(0f) }
    val offset = remember { Animatable(initialValue = 0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = isRevealed, contextMenuWidth) {
        if(isRevealed) {
            offset.animateTo(contextMenuWidth)
        } else {
            offset.animateTo(0f)
        }
    }

    Box(modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Box(modifier = Modifier.fillMaxWidth().listItem(listPosition, Color.Transparent)) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .onSizeChanged { contextMenuWidth = -it.width.toFloat() },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                actions()
            }
        }
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .pointerInput(contextMenuWidth) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                val newOffset = (offset.value + dragAmount)
                                    .coerceIn(contextMenuWidth, 0f)
                                offset.snapTo(newOffset)
                            }
                        },
                        onDragEnd = {
                            when {
                                offset.value <= contextMenuWidth / 2f -> {
                                    scope.launch {
                                        offset.animateTo(contextMenuWidth)
                                        onExpanded()
                                    }
                                }

                                else -> {
                                    scope.launch {
                                        offset.animateTo(0f)
                                        onCollapsed()
                                    }
                                }
                            }
                        }
                    )
                },
        ) {
            content(listPosition)
        }
    }
}

@Composable
fun ActionIcon(
    onClick: () -> Unit,
    backgroundColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = Color.White
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxHeight()
            .background(Color.Transparent)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = backgroundColor,
        )
    }
}