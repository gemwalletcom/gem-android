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
import com.gemwallet.android.ui.theme.paddingHalfSmall

private val normalTopPadding = 8.dp
private val normalBottomPadding = 8.dp

private val bigRound = 16.dp
private val smallRound = 2.dp
private val itemPadding = 1.dp

private val firstItemShape = RoundedCornerShape(topStart = bigRound, topEnd = bigRound, bottomStart = smallRound, bottomEnd = smallRound)
private val lastItemShape = RoundedCornerShape(bottomStart = bigRound, bottomEnd = bigRound, topStart = smallRound, topEnd = smallRound)

private val middleItemShape = RoundedCornerShape(smallRound)
private val singleItemShape = RoundedCornerShape(bigRound)

@Composable
fun Modifier.listItem(
    position: ListPosition = ListPosition.Single,
    background: Color = MaterialTheme.colorScheme.background,
    paddingVertical: Dp? = null,
    paddingHorizontal: Dp? = null,
): Modifier =
    padding(horizontal = paddingHorizontal ?: bigRound) then
    when (position) {
        ListPosition.Subhead -> this.padding(top = paddingVertical ?: normalTopPadding, bottom = paddingHalfSmall)
        ListPosition.First -> this.padding(top = paddingVertical ?: normalTopPadding).clip(firstItemShape)
        ListPosition.Middle -> this.padding(top = paddingVertical ?: itemPadding).clip(middleItemShape)
        ListPosition.Single -> this.padding(top = paddingVertical ?: normalTopPadding, bottom = paddingVertical ?: normalBottomPadding).clip(singleItemShape)
        ListPosition.Last -> this.padding(top = paddingVertical ?: itemPadding, bottom = normalBottomPadding).clip(lastItemShape)
    } then when (position) {
        ListPosition.Subhead -> Modifier
        else -> Modifier.background(background)
    }
