package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.ext.type
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ConfirmParams
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.EVMChain
import com.wallet.core.primitives.NFTType
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

fun EVMChain.Companion.encodeNFT(params: ConfirmParams.NftParams): String {
    return when (params.nftAsset.tokenType) {
        NFTType.ERC721 -> {
            val function = EthereumAbiFunction("safeTransferFrom")
            function.addParamAddress(params.from.address.decodeHex(), false)
            function.addParamAddress(params.destination.address.decodeHex(), false)
            function.addParamUInt256(params.nftAsset.tokenId.toBigInteger().abs().toByteArray(), false)
            EthereumAbi.encode(function)
        }
        NFTType.ERC1155 -> {
            val function = EthereumAbiFunction("safeTransferFrom")
            function.addParamAddress(params.from.address.decodeHex(), false)
            function.addParamAddress(params.destination.address.decodeHex(), false)
            function.addParamUInt256(params.nftAsset.tokenId.toBigInteger().abs().toByteArray(), false)
            function.addParamUInt256(BigInteger.ONE.toByteArray(), false)
            function.addParamBytes(byteArrayOf(), false)
            EthereumAbi.encode(function)
        }
        else -> throw IllegalArgumentException("Not supported")
    }.toHexString()
}