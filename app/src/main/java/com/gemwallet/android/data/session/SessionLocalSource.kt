package com.gemwallet.android.data.session

import com.wallet.core.primitives.Currency

interface SessionLocalSource {

    fun reset()

    fun setWallet(walletId: String)

    fun getWalletId(): String?

    fun setCurrency(currency: String)

    fun getCurrency(): Currency
}