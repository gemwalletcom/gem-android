/**
 * Generated by typeshare 1.9.2
 */

@file:NoLiveLiterals

package com.wallet.core.blockchain.xrp.models

import androidx.compose.runtime.NoLiveLiterals
import kotlinx.serialization.Serializable

@Serializable
data class XRPResult<T> (
	val result: T
)

@Serializable
data class XRPAccount (
	val Balance: String,
	val Sequence: Int
)

@Serializable
data class XRPAccountResult (
	val account_data: XRPAccount
)

@Serializable
data class XRPDrops (
	val median_fee: String
)

@Serializable
data class XRPFee (
	val drops: XRPDrops
)

@Serializable
data class XRPTransaction (
	val hash: String
)

@Serializable
data class XRPTransactionBroadcast (
	val accepted: Boolean,
	val engine_result_message: String? = null,
	val tx_json: XRPTransaction? = null
)

@Serializable
data class XRPTransactionStatus (
	val status: String
)

