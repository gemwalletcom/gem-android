/**
 * Generated by typeshare 1.13.2
 */

package com.wallet.core.blockchain.aptos

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class AptosSignature (
	val type: String,
	val public_key: String? = null,
	val signature: String? = null
)

@Serializable
data class AptosTransaction (
	val success: Boolean,
	val gas_used: String,
	val gas_unit_price: String
)

@Serializable
data class AptosTransactionBroacast (
	val hash: String
)

@Serializable
data class AptosTransactionPayload (
	val arguments: List<String>,
	val function: String,
	val type: String,
	val type_arguments: List<String>
)

@Serializable
data class AptosTransactionSimulation (
	val expiration_timestamp_secs: String,
	val gas_unit_price: String,
	val max_gas_amount: String,
	val payload: AptosTransactionPayload,
	val sender: String,
	val sequence_number: String,
	val signature: AptosSignature
)

