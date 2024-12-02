package com.gemwallet.android.features.receive.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.features.receive.navigation.assetIdArg
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReceiveViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val asset = savedStateHandle.getStateFlow(assetIdArg, "")
        .map { it.toAssetId() }
        .filterNotNull()
        .flatMapLatest { assetsRepository.getAssetInfo(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setVisible() {
        val assetId = asset.value?.asset?.id ?: return
        val session = sessionRepository.getSession() ?: return
        val account = session.wallet.getAccount(assetId.chain) ?: return

        viewModelScope.launch(Dispatchers.IO) {
            assetsRepository.switchVisibility(session.wallet.id, account, assetId, true, session.currency)
        }
    }
}