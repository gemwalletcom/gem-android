package com.gemwallet.android.blockchain.operators.walletcore

import com.gemwallet.android.blockchain.operators.CreateAccountOperator
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import wallet.core.jni.Derivation
import wallet.core.jni.HDWallet

class WCCreateAccountOperator : CreateAccountOperator {

    override fun invoke(data: String, chain: Chain): Account {
        val hdWallet = HDWallet(data, "")
        val coinType = WCChainTypeProxy().invoke(chain = chain)
        val address = if (chain == Chain.Solana) {
            hdWallet.getAddressDerivation(coinType, Derivation.SOLANASOLANA)
        } else {
            hdWallet.getAddressForCoin(coinType)
        }
        val extendedPublicKey = if (chain == Chain.Solana) {
            hdWallet.getExtendedPublicKeyDerivation(coinType.purpose(), coinType, Derivation.SOLANASOLANA, coinType.xpubVersion())
        } else {
            hdWallet.getExtendedPublicKey(coinType.purpose(), coinType, coinType.xpubVersion())
        }
        return Account(
            chain = chain,
            address = address,
            derivationPath = coinType.derivationPath(),
            extendedPublicKey = extendedPublicKey
        )
    }
}