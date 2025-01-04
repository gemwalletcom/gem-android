package com.gemwallet.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.components.designsystem.padding12
import com.gemwallet.android.ui.components.designsystem.padding16

@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    dividerShowed: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    subtitle: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.size(padding16))
        leading?.let {
            it()
            Spacer(modifier = Modifier.size(padding16))
        }
        Box(modifier = Modifier.heightIn(72.dp).weight(1f)) {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = padding12, end = padding16, bottom = padding12)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                ) {
                    title?.invoke()
                    subtitle?.let {
                        Spacer(modifier = Modifier.height(2.dp))
                        it()
                    }
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    trailing?.let {
                        Spacer(modifier = Modifier.size(padding16))
                        it()
                    }
                }
            }
            if (dividerShowed) {
                HorizontalDivider(Modifier.align(Alignment.BottomStart), thickness = 0.4.dp)
            }
        }
    }
}