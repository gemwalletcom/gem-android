package com.gemwallet.android.features.settings.aboutus.views

import androidx.lifecycle.ViewModel
import com.gemwallet.android.data.repositoreis.config.UserConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AboutUsViewModel @Inject constructor(
    private val userConfig: UserConfig,
) : ViewModel() {

    fun developEnable() {
        userConfig.developEnabled(!userConfig.developEnabled())
    }

}