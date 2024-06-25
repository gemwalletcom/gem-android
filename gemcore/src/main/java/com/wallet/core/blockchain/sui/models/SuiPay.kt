/**
 * Generated by typeshare 1.9.2
 */

@file:NoLiveLiterals

package com.wallet.core.blockchain.sui.models

import androidx.compose.runtime.NoLiveLiterals
import kotlinx.serialization.Serializable

@Serializable
data class SuiPay (
	val txBytes: String
)

@Serializable
data class SuiPayRequest (
	val senderAddress: String,
	val recipientAddress: String,
	val coins: List<String>,
	val gas: String? = null,
	val amount: String,
	val gasBudget: String
)

@Serializable
data class SuiAddStakeRequest (
	val senderAddress: String,
	val validatorAddress: String,
	val coins: List<String>,
	val amount: String,
	val gasBudget: String
)

@Serializable
data class SuiUnstakeRequest (
	val senderAddress: String,
	val delegationId: String,
	val gasBudget: String
)

@Serializable
data class SuiMoveCallRequest (
	val senderAddress: String,
	val objectId: String,
	val module: String,
	val function: String,
	val arguments: List<String>,
	val gasBudget: String
)

@Serializable
data class SuiSplitCoinRequest (
	val senderAddress: String,
	val coin: String,
	val splitAmounts: List<String>,
	val gasBudget: String
)

@Serializable
data class SuiBatchRequest (
	val senderAddress: String,
	val gasBudget: String
)

@Serializable
data class SuiBroadcastTransaction (
	val digest: String
)

@Serializable
data class SuiStake (
	val stakedSuiId: String,
	val status: String,
	val principal: String,
	val stakeRequestEpoch: String,
	val stakeActiveEpoch: String,
	val estimatedReward: String? = null
)

@Serializable
data class SuiStakeDelegation (
	val validatorAddress: String,
	val stakingPool: String,
	val stakes: List<SuiStake>
)

@Serializable
data class SuiSystemState (
	val epoch: String,
	val epochStartTimestampMs: String,
	val epochDurationMs: String
)

@Serializable
data class SuiValidator (
	val address: String,
	val apy: Double
)

@Serializable
data class SuiValidators (
	val apys: List<SuiValidator>
)

@Serializable
data class SuiCoinMetadata (
	val decimals: Int,
	val name: String,
	val symbol: String
)

