package com.gemwallet.android.features.settings.currency.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.wallet.core.primitives.Currency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrenciesViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
) : ViewModel() {
    val defaultCurrency: List<Currency> = listOf(
        Currency.USD,
        Currency.EUR,
        Currency.GBP,
        Currency.CNY,
        Currency.JPY,
        Currency.INR,
        Currency.RUB,
    )

    fun getDefaultCurrencies(): List<Currency> {
        val current = getCurrency()
        return if (defaultCurrency.contains(current)) {
            defaultCurrency
        } else {
            listOf(current) + defaultCurrency
        }
    }

    fun getCurrencies(): List<Currency> {
        return (Currency.entries.toSet() - defaultCurrency.toSet()).toList()
    }

    fun getCurrency(): Currency {
        return sessionRepository.getSession()?.currency ?: Currency.USD
    }

    fun setCurrency(currency: Currency) {
        viewModelScope.launch(Dispatchers.IO) {
            sessionRepository.setCurrency(currency)
            assetsRepository.clearPrices()
            assetsRepository.updatePrices(currency = currency)
        }
    }
}