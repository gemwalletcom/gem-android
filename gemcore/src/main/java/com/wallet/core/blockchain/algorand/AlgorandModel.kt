/**
 * Generated by typeshare 1.12.0
 */

package com.wallet.core.blockchain.algorand

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class AlgorandAccountAccount (
	@SerialName("amount")
	val amount: Long,
	@SerialName("asset-id")
	val asset_id: Int
)

@Serializable
data class AlgorandAccount (
	@SerialName("amount")
	val amount: Long,
	@SerialName("assets")
	val assets: List<AlgorandAccountAccount>,
	@SerialName("min-balance")
	val min_balance: Int
)

@Serializable
data class AlgorandAsset (
	@SerialName("decimals")
	val decimals: Int,
	@SerialName("name")
	val name: String,
	@SerialName("unit-name")
	val unit_name: String
)

@Serializable
data class AlgorandAssetResponse (
	val params: AlgorandAsset
)

@Serializable
data class AlgorandTransactionBroadcast (
	val txId: String? = null,
	val message: String? = null
)

@Serializable
data class AlgorandTransactionParams (
	@SerialName("min-fee")
	val min_fee: Int,
	@SerialName("genesis-id")
	val genesis_id: String,
	@SerialName("genesis-hash")
	val genesis_hash: String,
	@SerialName("last-round")
	val last_round: Int
)

@Serializable
data class AlgorandTransactionStatus (
	@SerialName("confirmed-round")
	val confirmed_round: Int
)

@Serializable
data class AlgorandVersions (
	@SerialName("genesis-id")
	val genesis_id: String,
	@SerialName("genesis-hash")
	val genesis_hash: String
)
