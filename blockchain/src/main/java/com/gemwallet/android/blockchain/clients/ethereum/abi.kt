package com.gemwallet.android.blockchain.clients.ethereum

import com.wallet.core.primitives.Chain
import wallet.core.jni.AnyAddress
import wallet.core.jni.CoinType
import wallet.core.jni.EthereumAbi
import wallet.core.jni.EthereumAbiFunction
import wallet.core.jni.EthereumChainID
import wallet.core.jni.proto.Ethereum
import java.math.BigInteger

fun encodeApprove(spender: String): ByteArray {
    val function = EthereumAbiFunction("approve")
    function.addParamAddress(AnyAddress(spender, CoinType.ETHEREUM).data(), false)
    function.addParamUInt256(BigInteger("2").pow(255).dec().toByteArray(), false)
    return EthereumAbi.encode(function)
}