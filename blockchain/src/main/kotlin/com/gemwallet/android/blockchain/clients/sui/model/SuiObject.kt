package com.gemwallet.android.blockchain.clients.sui.model

import kotlinx.serialization.Serializable

@Serializable
data class SuiObject(
    val data: Data,
) {
    @Serializable
    data class Data(
        val objectId: String,
        val version: String,
        val digest: String,
    )
}