package com.gemwallet.android.data.repositoreis.bridge

import com.reown.walletkit.client.Wallet

class WalletConnectEvent(
    val model: Wallet.Model,
    val verifyContext: Wallet.Model.VerifyContext? = null,
)