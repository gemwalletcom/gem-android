package com.gemwallet.android.data.repositoreis.bridge

import com.wallet.core.primitives.Account

class WalletConnectAccount(
    val address: String,
    val namespace: String,
    val reference: String,
    val methods: List<String>,
) {

    companion object {
        fun create(account: Account): WalletConnectAccount? {
            val namespace = account.chain.getNameSpace() ?: return null
            val reference = account.chain.getReference() ?: return null
            val methods = namespace.methods.map { it.string }
            return WalletConnectAccount(account.address, namespace.string, reference, methods)
        }
    }
}