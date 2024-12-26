package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.ext.type
import com.gemwallet.android.math.decodeHex
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.EVMChain
import uniffi.gemstone.Config
import wallet.core.jni.AnyAddress
import wallet.core.jni.EthereumAbi
import wallet.core.jni.EthereumAbiFunction
import java.math.BigInteger

fun EVMChain.Companion.encodeTransactionData(
    assetId: AssetId,
    memo: String?,
    amount: BigInteger,
    destinationAddress: String,
): ByteArray {
    return if (assetId.type() != AssetSubtype.NATIVE && memo.isNullOrEmpty()) {
        val abiFn = EthereumAbiFunction("transfer").apply {
            addParamAddress(AnyAddress(destinationAddress, WCChainTypeProxy().invoke(assetId.chain)).data(), false)
            addParamUInt256(amount.toByteArray(), false)
        }
        EthereumAbi.encode(abiFn)
    } else {
        memo?.decodeHex() ?: byteArrayOf()
    }
}

fun EVMChain.Companion.getDestinationAddress(
    assetId: AssetId,
    destinationAddress: String,
): String {
    return when (assetId.type()) {
        AssetSubtype.NATIVE -> destinationAddress
        AssetSubtype.TOKEN -> assetId.tokenId!!
    }
}

fun EVMChain.isOpStack(): Boolean {
    return Config().getEvmChainConfig(string).isOpstack
}