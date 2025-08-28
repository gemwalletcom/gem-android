package com.gemwallet.features.settings.security.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.config.UserConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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

    val lockInterval = userConfig.getLockInterval()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 1)

    fun authRequired(): Boolean {
        return userConfig.authRequired()
    }

    fun setAuthRequired(required: Boolean) {
        userConfig.setAuthRequired(required)
    }

    fun setLockInterval(minutes: Int) = viewModelScope.launch(Dispatchers.IO) {
        userConfig.setLockInterval(minutes)
    }

    fun setHideBalances() {
        viewModelScope.launch {
            userConfig.hideBalances()
        }
    }
}