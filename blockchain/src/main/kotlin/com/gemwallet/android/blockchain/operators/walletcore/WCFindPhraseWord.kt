package com.gemwallet.android.blockchain.operators.walletcore

import com.gemwallet.android.blockchain.operators.FindPhraseWord

class WCFindPhraseWord : FindPhraseWord {
    override fun invoke(query: String): List<String> {
        val words = wallet.core.jni.Mnemonic.suggest(query).split(" ")
        if (words.size == 1 && words.first().isNullOrEmpty()) {
            return emptyList()
        } else  {
            return words
        }
    }
}