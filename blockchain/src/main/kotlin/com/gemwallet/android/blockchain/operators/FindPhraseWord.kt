package com.gemwallet.android.blockchain.operators

interface FindPhraseWord {
    operator fun invoke(query: String): List<String>
}