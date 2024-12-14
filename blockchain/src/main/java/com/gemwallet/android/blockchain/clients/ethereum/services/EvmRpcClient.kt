package com.gemwallet.android.blockchain.clients.ethereum.services

import com.gemwallet.android.blockchain.clients.ethereum.services.EvmRpcClient.EvmNumber
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.math.hexToBigInteger
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import wallet.core.jni.EthereumAbiValue
import java.lang.reflect.Type
import java.math.BigInteger

interface EvmRpcClient :
    EvmCallService,
    EvmBalancesService,
    EvmFeeService,
    EvmNodeStatusService,
    EvmBroadcastService,
    EvmTransactionsService
{
    class EvmNumber(
        val value: BigInteger?,
    )

    class TokenBalance(
        val value: BigInteger?,
    )

    class BalanceDeserializer : JsonDeserializer<EvmNumber> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): EvmNumber {
            return EvmNumber(
                try {
                    json?.asString?.hexToBigInteger()
                } catch (_: Throwable) {
                    null
                }
            )
        }
    }

    class TokenBalanceDeserializer : JsonDeserializer<TokenBalance> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): TokenBalance {
            return TokenBalance(
                try {
                    EthereumAbiValue.decodeUInt256(json?.asString?.decodeHex()).toBigIntegerOrNull()
                } catch (_: Throwable) {
                    null
                }
            )
        }

    }
}