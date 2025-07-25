/**
 * Generated by typeshare 1.13.2
 */

package com.wallet.core.blockchain.cosmos

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class CosmosAccount (
	val account_number: String,
	val sequence: String
)

@Serializable
data class CosmosAccountResponse<T> (
	val account: T
)

@Serializable
data class CosmosBalance (
	val denom: String,
	val amount: String
)

@Serializable
data class CosmosBalances (
	val balances: List<CosmosBalance>
)

@Serializable
data class CosmosInjectiveAccount (
	val base_account: CosmosAccount
)

