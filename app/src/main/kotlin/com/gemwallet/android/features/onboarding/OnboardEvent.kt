package com.gemwallet.android.features.onboarding

sealed interface OnboardEvent {
    data object ImportClick : OnboardEvent

    data object CreateClick : OnboardEvent
}