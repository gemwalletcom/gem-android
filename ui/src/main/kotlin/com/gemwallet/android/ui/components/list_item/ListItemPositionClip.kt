package com.gemwallet.android.ui.components.list_item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.models.ListPosition

private val bigRound = 16.dp
private val smallRound = 2.dp
private val itemPadding = 1.dp

private val firstItemShape = RoundedCornerShape(topStart = bigRound, topEnd = bigRound, bottomStart = smallRound, bottomEnd = smallRound)
private val lastItemShape = RoundedCornerShape(bottomStart = bigRound, bottomEnd = bigRound, topStart = smallRound, topEnd = smallRound)

private val middleItemShape = RoundedCornerShape(smallRound)
private val singleItemShape = RoundedCornerShape(bigRound)

@Composable
fun Modifier.listItem(
    position: ListPosition,
    background: Color = MaterialTheme.colorScheme.background,
    padding: Dp? = null,
): Modifier =
    padding(horizontal = bigRound) then
    when (position) {
        ListPosition.First -> this.padding(top = padding ?: bigRound).clip(firstItemShape)
        ListPosition.Middle -> this.padding(top = padding ?: itemPadding).clip(middleItemShape)
        ListPosition.Single -> this.padding(top = padding ?: bigRound, bottom = padding ?: bigRound).clip(singleItemShape)
        ListPosition.Last -> this.padding(top = padding ?: itemPadding, bottom = padding ?: bigRound).clip(lastItemShape)
    }
    .background(background)