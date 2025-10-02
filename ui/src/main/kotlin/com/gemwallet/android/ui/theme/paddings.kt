package com.gemwallet.android.ui.theme

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val headerIconSize = 64.dp
val headerSupportIconSize = 24.dp
val listItemIconSize = 44.dp
val listItemSupportIconSize = 18.dp
val iconSize = 32.dp
val trailingIconMedium = 24.dp
val trailingIconSmall = 16.dp

private val space2 = 2.dp
private val space6 = 6.dp
val space4 = 4.dp
val space8 = 8.dp
private val space16 = 16.dp

val padding4 = 4.dp
val padding8 = 8.dp
val padding16 = 16.dp
val padding12 = 12.dp
val paddingDefault = 16.dp
val paddingLarge = 32.dp

val mainActionHeight = 48.dp

fun Modifier.smallPadding(): Modifier {
    return padding(4.dp)
}

fun Modifier.normalPadding(): Modifier {
    return padding(8.dp)
}

fun Modifier.middlePadding(): Modifier {
    return padding(12.dp)
}

fun Modifier.defaultPadding(): Modifier {
    return padding(16.dp)
}

fun Modifier.largePadding(): Modifier {
    return padding(32.dp)
}

@Composable
fun Spacer2() {
    Spacer(modifier = Modifier.size(space2))
}

@Composable
fun Spacer4() {
    Spacer(modifier = Modifier.size(space4))
}

@Composable
fun Spacer6() {
    Spacer(modifier = Modifier.size(space6))
}

@Composable
fun Spacer8() {
    Spacer(modifier = Modifier.size(space8))
}

@Composable
fun Spacer16() {
    Spacer(modifier = Modifier.size(space16))
}

@Composable
fun Stub() {
    Spacer(modifier = Modifier.size(0.dp))
}

fun LazyListScope.listSpacerBig() {
    item {
        Spacer16()
    }
}