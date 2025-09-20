package com.gemwallet.android.data.repositoreis.bridge

import com.gemwallet.android.ext.toChain
import com.gemwallet.android.ext.toChainType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChainType
import com.wallet.core.primitives.WalletConnectionMethods
import uniffi.gemstone.WalletConnectNamespace

enum class ChainNamespace(val string: String, val methods: List<WalletConnectionMethods>) {
    Eip155(
        "eip155",
        listOf(
            WalletConnectionMethods.EthChainId,
            WalletConnectionMethods.EthSign,
            WalletConnectionMethods.PersonalSign,
            WalletConnectionMethods.EthSignTypedData,
            WalletConnectionMethods.EthSignTypedDataV4,
            WalletConnectionMethods.EthSendTransaction,
            WalletConnectionMethods.EthSendTransaction,
            WalletConnectionMethods.WalletAddEthereumChain,
            WalletConnectionMethods.WalletSwitchEthereumChain,
            WalletConnectionMethods.EthSendRawTransaction,
        )
    ),
    Solana(
        Chain.Solana.string,
        listOf(
            WalletConnectionMethods.SolanaSignTransaction,
            WalletConnectionMethods.SolanaSignMessage,
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
        WalletConnectNamespace().getChain(chainId[0], chainId[1])?.toChain()
    } else {
        null
    }
}
