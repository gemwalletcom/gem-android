package com.gemwallet.android.features.settings.develop.viewmodels

import androidx.lifecycle.ViewModel
import com.gemwallet.android.cases.device.GetDeviceIdCase
import com.gemwallet.android.cases.device.GetPushTokenCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DevelopViewModel @Inject constructor(
    private val getDeviceIdCase: GetDeviceIdCase,
    private val getPushTokenCase: GetPushTokenCase,
) : ViewModel() {

    fun getDeviceId(): String {
        return getDeviceIdCase.getDeviceId()
    }

    fun getPushToken(): String {
        return getPushTokenCase.getPushToken()
    }
}