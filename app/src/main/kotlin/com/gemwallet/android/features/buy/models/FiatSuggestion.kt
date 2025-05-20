package com.gemwallet.android.features.buy.models

sealed class FiatSuggestion(open val text: String, open val value: Double) {
    class SuggestionAmount(override val text: String, override val value: Double) :
        FiatSuggestion(text, value)

    data class SuggestionPercent(override val text: String, override val value: Double) :
        FiatSuggestion(text, value)

    data object RandomAmount : FiatSuggestion("Random", 0.0)

    data object MaxAmount : FiatSuggestion("Max", 100.0)
}