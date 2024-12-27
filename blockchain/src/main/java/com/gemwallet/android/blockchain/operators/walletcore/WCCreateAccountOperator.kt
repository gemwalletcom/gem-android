package com.gemwallet.android.blockchain.operators.walletcore

import com.gemwallet.android.blockchain.operators.CreateAccountOperator
import com.gemwallet.android.math.decodeHex
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WalletType
import wallet.core.jni.AnyAddress
import wallet.core.jni.Derivation
import wallet.core.jni.HDWallet
import wallet.core.jni.PrivateKey

class WCCreateAccountOperator : CreateAccountOperator {

    override fun invoke(walletType: WalletType, data: String, chain: Chain): Account = when (walletType) {
        WalletType.multicoin,
        WalletType.single -> createFromPhrase(data, chain)
        WalletType.private_key -> createFromPrivateKey(data, chain)
        WalletType.view -> throw IllegalArgumentException()
    }

    private fun createFromPhrase(data: String, chain: Chain): Account {
        val hdWallet = HDWallet(data, "")
        val coinType = WCChainTypeProxy().invoke(chain = chain)
        val address = when (chain) {
            Chain.Solana -> hdWallet.getAddressDerivation(coinType, Derivation.SOLANASOLANA)
            Chain.BitcoinCash -> hdWallet.getAddressForCoin(coinType)
                .replaceFirst("${Chain.BitcoinCash.string}:", "")
            else -> hdWallet.getAddressForCoin(coinType)
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

    private fun createFromPrivateKey(data: String, chain: Chain): Account {
        val coinType = WCChainTypeProxy().invoke(chain = chain)
        val privateKey = PrivateKey(data.decodeHex())
        val publicKey = privateKey.getPublicKey(coinType)
        val address = AnyAddress(publicKey, coinType).description()
        return Account(
            chain = chain,
            address = address,
            derivationPath = coinType.derivationPath(),
            extendedPublicKey = ""
        )
    }
}