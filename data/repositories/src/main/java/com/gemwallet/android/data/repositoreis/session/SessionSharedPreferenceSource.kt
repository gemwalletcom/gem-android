package com.gemwallet.android.data.repositoreis.session

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.wallet.core.primitives.Currency
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Deprecated("Use for migration only")
class SessionSharedPreferenceSource @Inject constructor(
    private val context: Context
) {
    fun getWalletId(): String? = getString(Props.WalletId)

    fun getCurrency(): Currency = Currency.values().first { it.string == getString(Props.Currency, Currency.USD.string)!! }

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