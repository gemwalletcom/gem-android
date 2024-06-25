/**
 * Generated by typeshare 1.9.2
 */

@file:NoLiveLiterals

package com.wallet.core.primitives

import androidx.compose.runtime.NoLiveLiterals
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class DelegationState(val string: String) {
	@SerialName("active")
	Active("active"),
	@SerialName("pending")
	Pending("pending"),
	@SerialName("undelegating")
	Undelegating("undelegating"),
	@SerialName("inactive")
	Inactive("inactive"),
	@SerialName("activating")
	Activating("activating"),
	@SerialName("deactivating")
	Deactivating("deactivating"),
	@SerialName("awaitingwithdrawal")
	AwaitingWithdrawal("awaitingwithdrawal"),
}

@Serializable
data class DelegationBase (
	val assetId: AssetId,
	val state: DelegationState,
	val balance: String,
	val shares: String,
	val rewards: String,
	val completionDate: Long? = null,
	val delegationId: String,
	val validatorId: String
)

@Serializable
data class DelegationValidator (
	val chain: Chain,
	val id: String,
	val name: String,
	val isActive: Boolean,
	val commision: Double,
	val apr: Double
)

@Serializable
data class Delegation (
	val base: DelegationBase,
	val validator: DelegationValidator,
	val price: Price? = null
)

