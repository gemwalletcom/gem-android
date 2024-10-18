package com.gemwallet.android.blockchain.clients.sui.model

data class SuiObject(
    val data: Data,
) {
    data class Data(
        val objectId: String,
        val version: String,
        val digest: String,
    )
}

data class SuiValue(
    val value: String?
)