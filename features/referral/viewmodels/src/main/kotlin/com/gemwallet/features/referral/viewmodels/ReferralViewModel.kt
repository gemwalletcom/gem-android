package com.gemwallet.features.referral.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.application.referral.coordinators.CreateReferral
import com.gemwallet.android.application.referral.coordinators.GetRewards
import com.gemwallet.android.application.referral.coordinators.Redeem
import com.gemwallet.android.application.referral.coordinators.UseReferralCode
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.referralChain
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Rewards
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReferralViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val walletsRepository: WalletsRepository,
    private val getRewards: GetRewards,
    private val redeem: Redeem,
    private val useReferralCode: UseReferralCode,
    private val createReferral: CreateReferral
) : ViewModel() {

    val currentWallet = MutableStateFlow<Wallet?>(null)
    val rewards = MutableStateFlow<Rewards?>(null)
    val inSync = MutableStateFlow(SyncType.Init)

    val availableWallets = walletsRepository.getAll().mapLatest { items ->
        items.filter { it.type == WalletType.multicoin }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val session = sessionRepository.session()
        .filterNotNull()
        .onEach { session ->
            currentWallet.update {
                if (it?.id == null || it.id == session.wallet.id) {
                    session.wallet
                } else {
                    it
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val referralAccount = currentWallet.filterNotNull().mapLatest { wallet ->
        wallet.getAccount(Chain.referralChain) ?: return@mapLatest null
    }
    .filterNotNull()
    .onEach { sync(it, SyncType.Init) }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setWallet(wallet: Wallet) {
        currentWallet.update { wallet }
    }

    fun sync() {
        sync(referralAccount.value ?: return, SyncType.Refresh)
    }

    private fun sync(account: Account, type: SyncType) = viewModelScope.launch(Dispatchers.IO) {
        inSync.update { type }
        val rewards = try {
            getRewards.getRewards(account.address)
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
}