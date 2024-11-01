package com.gemwallet.android.data.repositoreis.bridge

import android.util.Log
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object WalletConnectDelegate : Web3Wallet.WalletDelegate, CoreClient.CoreDelegate {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _walletEvents: MutableSharedFlow<Wallet.Model> = MutableSharedFlow()
    val walletEvents: SharedFlow<Wallet.Model> = _walletEvents.asSharedFlow()

    init {
        CoreClient.setDelegate(this)
        Web3Wallet.setWalletDelegate(this)
    }

    override fun onAuthRequest(
        authRequest: Wallet.Model.AuthRequest,
        verifyContext: Wallet.Model.VerifyContext
    ) {
        Log.d("WALLET-CONNECT", "Auth request")
        scope.launch {
            _walletEvents.emit(authRequest)
        }
    }

    override fun onConnectionStateChange(state: Wallet.Model.ConnectionState) {
        Log.d("WALLET-CONNECT", "Connection state change")
        scope.launch {
            _walletEvents.emit(state)
        }
    }

    override fun onError(error: Wallet.Model.Error) {
        Log.d("WALLET-CONNECT", "On erro: ", error.throwable)
        scope.launch {
            _walletEvents.emit(error)
        }
    }

    override fun onProposalExpired(proposal: Wallet.Model.ExpiredProposal) {
    }

    override fun onRequestExpired(request: Wallet.Model.ExpiredRequest) {
    }

    override fun onSessionDelete(sessionDelete: Wallet.Model.SessionDelete) {
        Log.d("WALLET-CONNECT", "On session delete")
        scope.launch {
            _walletEvents.emit(sessionDelete)
        }
    }

    override fun onSessionExtend(session: Wallet.Model.Session) {
        Log.d("WALLET-CONNECT", "On session extended")
    }

    override fun onSessionProposal(
        sessionProposal: Wallet.Model.SessionProposal,
        verifyContext: Wallet.Model.VerifyContext
    ) {
        Log.d("WALLET-CONNECT", "On session proposal")
        scope.launch {
            _walletEvents.emit(sessionProposal)
        }
    }

    override fun onSessionRequest(
        sessionRequest: Wallet.Model.SessionRequest,
        verifyContext: Wallet.Model.VerifyContext
    ) {
        Log.d("WALLET-CONNECT", "On session request: $sessionRequest")
        scope.launch {
            _walletEvents.emit(sessionRequest)
        }
    }

    override fun onSessionSettleResponse(settleSessionResponse: Wallet.Model.SettledSessionResponse) {
        Log.d("WALLET-CONNECT", "On session settle response")
        scope.launch {
            _walletEvents.emit(settleSessionResponse)
        }
    }

    override fun onSessionUpdateResponse(sessionUpdateResponse: Wallet.Model.SessionUpdateResponse) {
        Log.d("WALLET-CONNECT", "On session update response")
        scope.launch {
            _walletEvents.emit(sessionUpdateResponse)
        }
    }

    override fun onPairingDelete(deletedPairing: Core.Model.DeletedPairing) {
        Log.d("WALLET-CONNECT", "On pairing delete")
//        scope.launch {
//            _coreEvents.emit(deletedPairing)
//        }
    }

    override fun onPairingExpired(expiredPairing: Core.Model.ExpiredPairing) {
        Log.d("WALLET-CONNECT", "onPairingExpired")
    }

    override fun onPairingState(pairingState: Core.Model.PairingState) {
        Log.d("WALLET-CONNECT", "onPairingExpired")
    }
}