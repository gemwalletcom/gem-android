package com.gemwallet.android.features.settings.develop.viewmodels

import androidx.lifecycle.ViewModel
import com.gemwallet.android.data.repositories.config.ConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DevelopViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
) : ViewModel() {

    fun getDeviceId(): String {
        return configRepository.getDeviceId()
    }

    fun getPushToken(): String {
        return configRepository.getPushToken()
    }
}