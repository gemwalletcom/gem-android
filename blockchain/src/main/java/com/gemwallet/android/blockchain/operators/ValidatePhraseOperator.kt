package com.gemwallet.android.blockchain.operators

interface ValidatePhraseOperator {
    operator fun invoke(data: String): Result<Boolean>
}

class InvalidWords(val words: List<String>): Exception()

object InvalidPhrase: Exception()