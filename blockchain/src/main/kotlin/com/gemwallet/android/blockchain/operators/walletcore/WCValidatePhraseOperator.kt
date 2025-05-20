package com.gemwallet.android.blockchain.operators.walletcore

import com.gemwallet.android.blockchain.operators.InvalidPhrase
import com.gemwallet.android.blockchain.operators.InvalidWords
import com.gemwallet.android.blockchain.operators.ValidatePhraseOperator
import wallet.core.jni.Mnemonic

class WCValidatePhraseOperator : ValidatePhraseOperator {
    override fun invoke(data: String): Result<Boolean> {
        val invalidWords = data.split(" ")
            .mapNotNull {
                if (Mnemonic.isValidWord(it)) {
                    return@mapNotNull null
                }
                it
            }
        if (invalidWords.isNotEmpty()) {
            return Result.failure(InvalidWords(invalidWords))
        }
        return if (Mnemonic.isValid(data)) {
            Result.success(true)
        } else {
            Result.failure(InvalidPhrase)
        }
    }
}