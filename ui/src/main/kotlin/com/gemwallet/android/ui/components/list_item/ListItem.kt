package com.gemwallet.android.ui.components.list_item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.Spacer2
import com.gemwallet.android.ui.theme.Stub
import com.gemwallet.android.ui.theme.padding12
import com.gemwallet.android.ui.theme.paddingDefault

@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    dividerShowed: Boolean = true,
    leading: (@Composable RowScope.() -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    subtitle: (@Composable () -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer16()
        leading?.let {
            it()
            Spacer16()
        }
        Box(modifier = Modifier.heightIn(72.dp).weight(1f)) { // Used to show correct divider
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = padding12, end = paddingDefault, bottom = padding12)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Stub()
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                ) {
                    title?.invoke()
                    subtitle?.let {
                        Spacer2()
                        it()
                    }
                }
                trailing?.let {
                    Spacer16()
                    it()
                }
            }
            if (dividerShowed) {
                HorizontalDivider(Modifier.align(Alignment.BottomStart), thickness = 0.4.dp)
            }
        }
    }
}