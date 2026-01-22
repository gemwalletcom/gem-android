package com.gemwallet.android.data.coordinates.wallet

import com.gemwallet.android.application.wallet.coordinators.WalletIdGenerator
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WalletType

class WalletIdGeneratorImpl : WalletIdGenerator {
    override fun generateWalletId(
        type: WalletType,
        priorityChain: Chain,
        priorityAddress: String
    ): String {
        require(priorityAddress.isNotEmpty()) { "Account address cannot be empty" }
        return when (type) {
            WalletType.Multicoin -> "${type.string}_$priorityAddress"
            WalletType.Single,
            WalletType.PrivateKey,
            WalletType.View -> "${type.string}_${priorityChain.string}_$priorityAddress"
        }
    }

}