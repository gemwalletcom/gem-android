package com.gemwallet.android.features.settings.security.viewmodels

import androidx.lifecycle.ViewModel
import com.gemwallet.android.data.repositoreis.config.UserConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val userConfig: UserConfig,
) : ViewModel() {

    fun authRequired(): Boolean {
        return userConfig.authRequired()
    }

    fun setAuthRequired(required: Boolean) {
        userConfig.setAuthRequired(required)
    }
}