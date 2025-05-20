package com.gemwallet.android.features.settings.security.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.config.UserConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val userConfig: UserConfig,
) : ViewModel() {

    val isHideBalances = userConfig.isHideBalances()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun authRequired(): Boolean {
        return userConfig.authRequired()
    }

    fun setAuthRequired(required: Boolean) {
        userConfig.setAuthRequired(required)
    }

    fun setHideBalances() {
        viewModelScope.launch {
            userConfig.hideBalances()
        }
    }
}