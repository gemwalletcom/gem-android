package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.rpc.model.JSONRpcMethod

enum class EvmMethod(val value: String) : JSONRpcMethod {
    GetBalance("eth_getBalance"),
    GetGasLimit("eth_estimateGas"),
    GetFeeHistory("eth_feeHistory"),
    GetChainId("eth_chainId"),
    GetNonce("eth_getTransactionCount"),
    Broadcast("eth_sendRawTransaction"),
    Call("eth_call"),
    GetTransaction("eth_getTransactionReceipt"),
    Sync("eth_syncing"),
    GetBlockNumber("eth_blockNumber"),
    ;

    override fun value(): String = value
}