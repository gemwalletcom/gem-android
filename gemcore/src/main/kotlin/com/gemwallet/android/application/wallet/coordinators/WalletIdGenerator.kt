package com.gemwallet.android.application.wallet.coordinators

import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WalletType

interface WalletIdGenerator {
    fun generateWalletId(type: WalletType, priorityChain: Chain, priorityAddress: String): String

    fun getPriorityAccount(accounts: List<Account>): Account? {
        require(accounts.isNotEmpty()) { "Accounts list cannot be empty" }
        return accounts.firstOrNull { it.chain == Chain.Ethereum } ?: accounts.firstOrNull()
    }
}