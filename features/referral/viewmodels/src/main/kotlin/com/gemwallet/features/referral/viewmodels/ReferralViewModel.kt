package com.gemwallet.features.referral.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.application.referral.coordinators.CreateReferral
import com.gemwallet.android.application.referral.coordinators.GetRewards
import com.gemwallet.android.application.referral.coordinators.Redeem
import com.gemwallet.android.application.referral.coordinators.UseReferralCode
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.wallet.core.primitives.RewardRedemptionOption
import com.wallet.core.primitives.Rewards
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReferralViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    walletsRepository: WalletsRepository,
    private val getRewards: GetRewards,
    private val redeem: Redeem,
    private val useReferralCode: UseReferralCode,
    private val createReferral: CreateReferral,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val referralCode = savedStateHandle.getStateFlow<String?>("code", null)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val currentWallet = MutableStateFlow<Wallet?>(null)
    val rewards = MutableStateFlow<Rewards?>(null)
    val inSync = MutableStateFlow(SyncType.Init)

    val availableWallets = walletsRepository.getAll().mapLatest { items ->
        items.filter { it.type == WalletType.Multicoin }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val session = sessionRepository.session()
        .filterNotNull()
        .combine(availableWallets) { session, availableWallets ->
            if (session.wallet.type != WalletType.Multicoin) {
                availableWallets.firstOrNull()
            } else {
                session.wallet
            }
        }
        .onEach { wallet ->
            currentWallet.update {
                if (it?.id == null || it.id == wallet?.id) {
                    wallet
                } else {
                    it
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val referralWallet = currentWallet.filterNotNull()
    .onEach { sync(it, SyncType.Init) }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setWallet(wallet: Wallet) {
        currentWallet.update { wallet }
    }

    fun sync() {
        sync(referralWallet.value ?: return, SyncType.Refresh)
    }

    private fun sync(wallet: Wallet, type: SyncType) = viewModelScope.launch(Dispatchers.IO) {
        inSync.update { type }
        val rewards = try {
            getRewards.getRewards(wallet.id)
        } catch (_: Exception) {
            null
        } finally {
            inSync.update { SyncType.None }
        }
        this@ReferralViewModel.rewards.update { rewards }
    }

    fun createReferral(username: String, callback: (Exception?) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        val rewards = try {
            val wallet = currentWallet.value ?: return@launch
            val response = createReferral.createReferral(username, wallet)
            callback(null)
            response
        } catch (err: Exception) {
            callback(err)
            null
        }
        this@ReferralViewModel.rewards.update { rewards }
    }

    fun useCode(code: String, callback: (Exception?) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val wallet = currentWallet.value ?: return@launch
            useReferralCode.useReferralCode(code, wallet)
            callback(null)
        } catch (err: Exception) {
            callback(err)
        }
    }

    fun redeem(option: RewardRedemptionOption, callback: (Throwable?) -> Unit) {
        val wallet = currentWallet.value ?: return
        val rewards = rewards.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                redeem.redeem(wallet, rewards, option)
                sync()
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            } catch (err: Throwable) {
                withContext(Dispatchers.Main) {
                    callback(err)
                }
            }
        }
    }

    fun cancelCode() {
        savedStateHandle["code"] = null
    }
}
