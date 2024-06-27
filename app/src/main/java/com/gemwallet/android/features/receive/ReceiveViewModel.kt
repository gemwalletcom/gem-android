package com.gemwallet.android.features.receive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.session.SessionRepository
import com.gemwallet.android.ext.getAccount
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceiveViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
) : ViewModel() {

    private val state = MutableStateFlow(ReceiveViewModelState())
    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ReceiveUIState.Success())

    fun onAccount(id: AssetId) {
        viewModelScope.launch(Dispatchers.IO) {
            val session = sessionRepository.getSession()
            if (session == null) {
                sendFatalError("Can't find active wallet", ReceiveErrorIntent.Cancel)
                return@launch
            }

            val account = session.wallet.getAccount(id.chain)
            if (account == null) {
                sendFatalError("Asset doesn't find", ReceiveErrorIntent.Cancel)
                return@launch
            }

            val asset = assetsRepository.getById(session.wallet, id).getOrNull()?.firstOrNull()
            if (asset == null) {
                sendFatalError("Asset doesn't find", ReceiveErrorIntent.Cancel)
            } else {
                handleAsset(walletName = session.wallet.name, account, asset.asset)
            }
        }
    }

    fun setVisible() {
        val assetId = state.value.asset?.id ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val session = sessionRepository.getSession() ?: return@launch
            val account = session.wallet.getAccount(assetId.chain) ?: return@launch
            assetsRepository.switchVisibility(account, assetId, true, session.currency)
        }
    }

    private fun handleAsset(walletName: String, account: Account, asset: Asset) {
        state.update {
            ReceiveViewModelState(
                walletName = walletName,
                account = account,
                asset = asset,
            )
        }
    }

    private fun sendFatalError(message: String, intent: ReceiveErrorIntent) {
        state.update {
            ReceiveViewModelState(message = message, intent = intent)
        }
    }
}

data class ReceiveViewModelState(
    val message: String = "",
    val intent: ReceiveErrorIntent = ReceiveErrorIntent.None,
    val walletName: String = "",
    val account: Account? = null,
    val asset: Asset? = null,
) {
    fun toUIState(): ReceiveUIState = if (message.isNotEmpty()) {
        ReceiveUIState.Fatal(
            message = message,
            intent = intent,
        )
    } else {
        ReceiveUIState.Success(
            walletName = walletName,
            address = account?.address ?: "",
            assetTitle = asset?.name ?: "",
            assetSymbol = asset?.symbol ?: "",
            chain = account?.chain,
        )
    }
}

sealed interface ReceiveUIState {
    data class Success(
        val walletName: String = "",
        val address: String = "",
        val assetTitle: String = "",
        val assetSymbol: String = "",
        val chain: Chain? = null,
    ) : ReceiveUIState

    data class Fatal(
        val message: String,
        val intent: ReceiveErrorIntent,
    ) : ReceiveUIState
}

enum class ReceiveErrorIntent {
    None,
    Cancel,
}