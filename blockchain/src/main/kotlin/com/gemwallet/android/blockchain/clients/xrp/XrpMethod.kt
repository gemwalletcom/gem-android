package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.blockchain.rpc.model.JSONRpcMethod

enum class XrpMethod(val value: String) : JSONRpcMethod {
    Account("account_info"),
    Fee("fee"),
    ;

    override fun value(): String = value
}