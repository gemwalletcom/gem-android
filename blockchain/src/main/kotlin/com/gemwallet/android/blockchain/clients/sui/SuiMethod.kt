package com.gemwallet.android.blockchain.clients.sui

import com.gemwallet.android.blockchain.rpc.model.JSONRpcMethod

enum class SuiMethod(val value: String) : JSONRpcMethod {
    Coins("suix_getCoins"),
    GasPrice("suix_getReferenceGasPrice"),
    DryRun("sui_dryRunTransactionBlock"),
    Transaction("sui_getTransactionBlock"),
    GetObject("sui_getObject"),
    ;

    override fun value(): String = value
}