package com.gemwallet.android.data.repositoreis.bridge

import com.gemwallet.android.ext.toChainType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChainType
import com.wallet.core.primitives.WalletConnectionMethods
import uniffi.gemstone.WalletConnectNamespace

enum class ChainNamespace(val string: String, val methods: List<WalletConnectionMethods>) {
    Eip155(
        "eip155",
        listOf(
            WalletConnectionMethods.eth_chain_id,
            WalletConnectionMethods.eth_sign,
            WalletConnectionMethods.personal_sign,
            WalletConnectionMethods.eth_sign_typed_data,
            WalletConnectionMethods.eth_sign_typed_data_v4,
            WalletConnectionMethods.eth_sign_transaction,
            WalletConnectionMethods.eth_send_transaction,
            WalletConnectionMethods.wallet_add_ethereum_chain,
            WalletConnectionMethods.wallet_switch_ethereum_chain,
            WalletConnectionMethods.eth_send_raw_transaction,
        )
    ),
    Solana(
        Chain.Solana.string,
        listOf(
            WalletConnectionMethods.solana_sign_transaction,
            WalletConnectionMethods.solana_sign_message,
        )
    )
}

fun Chain.getChainNameSpace(): String? {
    return WalletConnectNamespace().getNamespace(string)
}

fun Chain.getNameSpace(): ChainNamespace? {
    return when (this.toChainType()) {
        ChainType.Ethereum -> ChainNamespace.Eip155
        ChainType.Solana -> ChainNamespace.Solana
        else -> return null
    }
}

fun Chain.getReference(): String? {
    return WalletConnectNamespace().getReference(string)
}

fun Chain.Companion.getNamespace(walletConnectChainId: String?): Chain? { // TODO: Use Reown call for parse
    val chainId = walletConnectChainId?.split(":")
    return if (!chainId.isNullOrEmpty() && chainId.size >= 2) {
        WalletConnectNamespace().getChain(chainId[0], chainId[1])?.let { namespace ->
            Chain.entries.firstOrNull { it.string == namespace }
        }
    } else {
        null
    }
}
