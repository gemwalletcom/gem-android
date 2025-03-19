/**
 * Generated by typeshare 1.13.2
 */

package com.wallet.core.blockchain.cosmos.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class CosmosBalance (
	val denom: String,
	val amount: String
)

@Serializable
data class CosmosBalances (
	val balances: List<CosmosBalance>
)

