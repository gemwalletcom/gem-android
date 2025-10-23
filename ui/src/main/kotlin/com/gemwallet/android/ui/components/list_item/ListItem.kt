package com.gemwallet.android.ui.components.list_item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.paddingDefault
import com.gemwallet.android.ui.theme.paddingMiddle
import kotlin.math.max

@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    listPosition: ListPosition,
    leading: (@Composable RowScope.() -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    subtitle: (@Composable () -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .listItem(position = listPosition)
            .then(
                modifier
                    .fillMaxWidth()
                    .padding(start = paddingDefault)
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(paddingDefault)
    ) {
        leading?.invoke(this)
        ListItemLayout(
            modifier = Modifier
                .heightIn(min = 72.dp)
                .padding(top = paddingMiddle, end = paddingDefault, bottom = paddingMiddle)
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.Start,
            ) {
                title?.invoke()
                subtitle?.invoke()
            }
            Row(
                modifier = Modifier.fillMaxHeight(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                trailing?.let {
                    Spacer16()
                    it()
                }
            }
        }
    }
}

@Composable
private fun ListItemLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->

        val placeable = if (measurables.size != 2) {
            measurables.map { it.measure(constraints) }
        } else {
            val height = measurables.maxOfOrNull { measurable -> measurable.maxIntrinsicHeight(constraints.minWidth) }
                ?: constraints.minHeight
            val widths = measurables.map { measurable -> measurable.maxIntrinsicWidth(height) }
            val totalWidth = widths.sum()

            if (totalWidth > constraints.maxWidth) {
                val (firstItemWidth, lastItemWidth) = if (widths.last() < constraints.maxWidth / 2) {
                    Pair(
                        max(0, constraints.maxWidth - widths.last()),
                        max(0, widths.last())
                    )
                } else {
                    Pair(
                        max(0, widths.first()),
                        max(0, constraints.maxWidth - widths.first()))
                }

                listOf(
                    measurables.first().measure(
                        constraints.copy(
                            minWidth = firstItemWidth,
                            maxWidth = firstItemWidth
                        )
                    ),
                    measurables.last().measure(
                        constraints.copy(
                            minWidth = lastItemWidth,
                            maxWidth = lastItemWidth
                        )
                    )
                )
            } else {
                val remainder = (constraints.maxWidth - totalWidth) / measurables.size
                measurables.mapIndexed { index, measurable ->
                    val width = widths[index] + remainder
                    measurable.measure(
                        constraints = constraints.copy(
                            minWidth = width,
                            maxWidth = width,
                            minHeight = height,
                            maxHeight = height,
                        )
                    )
                }
            }
        }

        layout(constraints.maxWidth, constraints.minHeight) {
            var xPosition = 0
            placeable.forEach { placeable ->
                placeable.place(x = xPosition, y = (constraints.minHeight - placeable.height) / 2)
                xPosition += placeable.width
            }
        }
    }
}

@Preview
@Composable
fun PreviewListItem() {
    MaterialTheme {
        ListItem(
            title = {
                Text(
                    "Some title",
                    overflow = TextOverflow.MiddleEllipsis,
                    maxLines = 1,
                )
            },
            listPosition = ListPosition.Single,
            trailing = {
                Text(
                    "Some_data_Some_data_Some_data_Some_data_Some_data_Some_data_Some_data_Some_data!",
                    overflow = TextOverflow.MiddleEllipsis,
                    maxLines = 1,
                )
            }
        )
    }
}