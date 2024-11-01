package com.gemwallet.android.data.repositoreis.bridge

import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WalletConnectionMethods

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

fun Chain.getNameSpace(): ChainNamespace? {
    return when (this) {
        Chain.Ethereum,
        Chain.SmartChain,
        Chain.Base,
        Chain.AvalancheC,
        Chain.Polygon,
        Chain.Arbitrum,
        Chain.OpBNB,
        Chain.Manta,
        Chain.Fantom,
        Chain.Gnosis,
        Chain.Optimism -> ChainNamespace.Eip155
        Chain.Solana -> ChainNamespace.Solana
        else -> return null
    }
}

fun Chain.getReference(): String? {
    return when (this) {
        Chain.Ethereum -> "1"
        Chain.SmartChain -> "56"
        Chain.Base -> "8453"
        Chain.AvalancheC -> "43114"
        Chain.Polygon -> "137"
        Chain.Arbitrum -> "42161"
        Chain.Optimism -> "10"
        Chain.OpBNB -> "204"
        Chain.Fantom -> "250"
        Chain.Gnosis -> "100"
        Chain.Manta -> "169"
        Chain.Blast -> "81457"
        Chain.Solana -> "4sGjMW1sUnHzSxGspuhpqLDx6wiyjNtZ"
        else -> return null
    }
}

fun Chain.Companion.findByNamespace(namespace: String, reference: String): Chain? {
    return Chain.entries.firstOrNull { it.getNameSpace()?.string == namespace && it.getReference() == reference }
}
