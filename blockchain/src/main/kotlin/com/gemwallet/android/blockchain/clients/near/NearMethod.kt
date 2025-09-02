package com.gemwallet.android.blockchain.clients.near

enum class NearMethod(val value: String) {
    Query("query"),
    LatestBlock("block"),
}