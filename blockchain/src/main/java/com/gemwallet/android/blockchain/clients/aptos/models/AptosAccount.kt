package com.gemwallet.android.blockchain.clients.aptos.models

import kotlinx.serialization.Serializable

@Serializable
data class AptosAccount(
    val sequence_number: String?,
    val message: String?,
    val error_code: String?,
)

const val aptosErrorCode = "account_not_found"