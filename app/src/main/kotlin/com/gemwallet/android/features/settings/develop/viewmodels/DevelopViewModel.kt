package com.gemwallet.android.features.settings.develop.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.device.GetDeviceIdCase
import com.gemwallet.android.cases.device.GetPushToken
import com.gemwallet.android.cases.transactions.ClearPendingTransactions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DevelopViewModel @Inject constructor(
    private val getDeviceIdCase: GetDeviceIdCase,
    private val getPushTokenCase: GetPushToken,
    private val clearPendingTransactions: ClearPendingTransactions,
) : ViewModel() {

    fun getDeviceId(): String {
        return getDeviceIdCase.getDeviceId()
    }

    fun getPushToken(): String {
        return getPushTokenCase.getPushToken()
    }

    fun resetTransactions() {
        viewModelScope.launch(Dispatchers.IO) {
            clearPendingTransactions.clearPending()
        }
    }
}