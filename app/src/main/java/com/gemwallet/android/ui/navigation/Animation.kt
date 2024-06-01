package com.gemwallet.android.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry

internal const val animEnterDuration = 280
internal const val animExitDuration = 350

val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Companion.Left,
        animationSpec = tween(animEnterDuration)
    )
}

val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Companion.Left,
        animationSpec = tween(animExitDuration)
    )
}

val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Companion.Right,
        animationSpec = tween(animEnterDuration)
    )
}

val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Companion.Right,
        animationSpec = tween(animExitDuration)
    )
}

val enterTabScreenTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    androidx.compose.animation.fadeIn(
        initialAlpha = 0.99f,
        animationSpec = snap(animEnterDuration)
    )
}

val exitTabScreenTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    androidx.compose.animation.fadeOut(
        targetAlpha = 0.99f,
        animationSpec = snap(animExitDuration)
    )
}

val popEnterTabScreenTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    androidx.compose.animation.fadeIn(
        animationSpec = tween(1)
    )
}

val popExitTabScreenTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    androidx.compose.animation.fadeOut(
        animationSpec = tween(1)
    )
}