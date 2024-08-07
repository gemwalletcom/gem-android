/**
 * Generated by typeshare 1.9.2
 */

@file:NoLiveLiterals

package com.wallet.core.blockchain.near.models

import androidx.compose.runtime.NoLiveLiterals
import kotlinx.serialization.*

@Serializable
data class NearAccount (
	val amount: String
)

@Serializable
data class NearAccountAccessKey (
	val nonce: Long
)

@Serializable
data class NearError (
	val message: String,
	val data: String? = null
)

@Serializable
data class NearRPCError (
	val error: NearError
)

@Serializable
data class NearBlockHeader (
	val hash: String,
	val height: Long
)

@Serializable
data class NearBlock (
	val header: NearBlockHeader
)

@Serializable
data class NearGasPrice (
	val gas_price: String
)

@Serializable
data class NearBroadcastTransaction (
	val hash: String
)

@Serializable
data class NearBroadcastResult (
	val final_execution_status: String,
	val transaction: NearBroadcastTransaction
)

