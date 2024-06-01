package com.gemwallet.android.blockchain.clients.near

enum class NearMethod(val value: String) {
    GasPrice("gas_price"),
    Query("query"),
    LatestBlock("block"),
    Transaction("tx"),
    Broadcast("send_tx"),
}