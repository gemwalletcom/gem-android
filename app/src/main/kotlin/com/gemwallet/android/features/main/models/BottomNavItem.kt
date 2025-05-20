package com.gemwallet.android.features.main.models

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavOptions

class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
    val badge: String? = null,
    val testTag: String,
    val navigate: NavController.(navOptions: NavOptions?) -> Unit,
)