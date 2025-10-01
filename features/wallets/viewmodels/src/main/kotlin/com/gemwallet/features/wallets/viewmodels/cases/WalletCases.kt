package com.gemwallet.features.wallets.viewmodels.cases

import com.gemwallet.android.domains.asset.getIconUrl
import com.gemwallet.android.ui.R
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType

// TODO: Out to ui-models

val Wallet.typeLabel: String
    get() = when (type) {
        WalletType.view,
        WalletType.private_key,
        WalletType.single -> accounts.firstOrNull()?.address ?: ""
        WalletType.multicoin -> "Multi-coin"
    }

val Wallet.icon: Any
    get() = if (accounts.size > 1) {
        R.drawable.multicoin_wallet
    } else {
        accounts.firstOrNull()?.chain?.getIconUrl() ?: ""
    }