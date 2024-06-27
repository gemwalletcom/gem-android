package com.gemwallet.android.data.repositories.session

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.wallet.core.primitives.Currency
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionSharedPreferenceSource @Inject constructor(
    private val context: Context
) : SessionLocalSource {
    override fun reset() {
        getStore().edit().remove(Props.WalletId.name).apply()
    }

    override fun setWallet(walletId: String) {
        setString(Props.WalletId, walletId)
    }

    override fun getWalletId(): String? = getString(Props.WalletId)


    override fun setCurrency(currency: String) {
        setString(Props.Currency, currency)
    }

    override fun getCurrency(): Currency = Currency.values().first { it.string == getString(Props.Currency, Currency.USD.string)!! }

    private fun getStore() = context.getSharedPreferences("session", MODE_PRIVATE)

    private fun setString(prop: Props, value: String?) {
        getStore().edit()
            .putString(prop.name, value)
            .apply()
    }

    private fun getString(prop: Props, default: String? = null): String? =
        getStore().getString(prop.name, default)

    private enum class Props() {
        WalletId,
        Currency,
    }
}