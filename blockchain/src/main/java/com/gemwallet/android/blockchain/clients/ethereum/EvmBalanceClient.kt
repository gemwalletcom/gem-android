package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.Balances
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import wallet.core.jni.EthereumAbi
import wallet.core.jni.EthereumAbiFunction

class EvmBalanceClient(
    private val chain: Chain,
    private val rpcClient: EvmRpcClient,
) : BalanceClient {

    override suspend fun getNativeBalance(address: String): Balances? {
        val available = rpcClient.getBalance(address)
            .fold({ Balances.create(AssetId(chain), it.result?.value ?: return null) }) { null }
        return when (chain) {
            Chain.SmartChain -> SmartchainStakeClient(rpcClient).getBalance(address, availableBalance = available);
            else -> available
        }
    }

    override suspend fun getTokenBalances(address: String, tokens: List<AssetId>): List<Balances> {
        if (tokens.isEmpty()) {
            return emptyList()
        }
        val result = mutableListOf<Balances>()
        for (token in tokens) {
            val data = "0x70a08231000000000000000000000000${address.removePrefix("0x")}"
            val contract = token.tokenId ?: continue
            val params = mapOf(
                "to" to contract,
                "data" to data,
            )
            val balance = rpcClient.callNumber(JSONRpcRequest.create(EvmMethod.Call, listOf(params, "latest")))
                .getOrNull()?.result?.value ?: continue
            result.add(Balances.create(token, available = balance))
        }
        return result
    }

    override fun maintainChain(): Chain = chain

    private fun encodeBalanceOf(address: String): String {
        val function = EthereumAbiFunction("balanceOf")
        val addressData = address.decodeHex()
        function.addParamAddress(addressData, false)
        function.addParamUInt256(byteArrayOf(), true)
        return EthereumAbi.encode(function).toHexString()
    }
}