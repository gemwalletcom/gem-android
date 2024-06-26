/**
 * Generated by typeshare 1.9.2
 */

@file:NoLiveLiterals

package com.wallet.core.blockchain.cosmos.models

import androidx.compose.runtime.NoLiveLiterals
import kotlinx.serialization.Serializable

@Serializable
data class CosmosBalance (
	val denom: String,
	val amount: String
)

@Serializable
data class CosmosBalances (
	val balances: List<CosmosBalance>
)

