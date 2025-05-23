/**
 * Generated by typeshare 1.13.2
 */

package com.wallet.core.blockchain.stellar

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class StellarBalance (
	val balance: String,
	val asset_type: String,
	val asset_code: String? = null,
	val asset_issuer: String? = null
)

@Serializable
data class StellarAccount (
	val sequence: String,
	val balances: List<StellarBalance>
)

@Serializable
data class StellarAsset (
	val asset_code: String,
	val asset_issuer: String,
	val contract_id: String? = null
)

@Serializable
data class StellarRecords<T> (
	val records: List<T>
)

@Serializable
data class StellarEmbedded<T> (
	val _embedded: StellarRecords<T>
)

@Serializable
data class StellarFeeCharged (
	val min: String,
	val p95: String
)

@Serializable
data class StellarFees (
	val last_ledger_base_fee: String,
	val fee_charged: StellarFeeCharged
)

@Serializable
data class StellarNodeStatus (
	val ingest_latest_ledger: Int,
	val network_passphrase: String
)

@Serializable
data class StellarTransactionBroadcast (
	val hash: String? = null,
	val title: String? = null
)

@Serializable
data class StellarTransactionStatus (
	val successful: Boolean,
	val fee_charged: String
)

