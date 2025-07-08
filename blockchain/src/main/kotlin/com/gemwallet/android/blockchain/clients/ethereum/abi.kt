package com.gemwallet.android.blockchain.clients.ethereum

import wallet.core.jni.EthereumAbi
import wallet.core.jni.EthereumAbiFunction
import java.math.BigInteger

fun encodeApprove(spender: ByteArray): ByteArray {
    val function = EthereumAbiFunction("approve")
    function.addParamAddress(spender, false)
    function.addParamUInt256(BigInteger("2").pow(255).dec().toByteArray(), false)
    return EthereumAbi.encode(function)
}