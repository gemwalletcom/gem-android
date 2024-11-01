package com.gemwallet.android.features.settings.security.viewmodels

import androidx.lifecycle.ViewModel
import com.gemwallet.android.data.repositoreis.config.ConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
) : ViewModel() {

    fun authRequired(): Boolean {
        return configRepository.authRequired()
    }

    fun setAuthRequired(required: Boolean) {
        configRepository.setAuthRequired(required)
    }
}