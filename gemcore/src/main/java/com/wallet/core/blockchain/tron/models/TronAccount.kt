/**
 * Generated by typeshare 1.13.2
 */

package com.wallet.core.blockchain.tron.models

import kotlinx.serialization.Serializable

@Serializable
data class TronAccountPermission (
	val threshold: Int
)

@Serializable
data class TronVote (
	val vote_address: String,
	val vote_count: Long
)

@Serializable
data class TronFrozen (
	val type: String? = null,
	val amount: Long? = null
)

@Serializable
data class TronUnfrozen (
	val unfreeze_amount: Long? = null,
	val unfreeze_expire_time: Long? = null
)

@Serializable
data class TronAccount (
	val balance: Long? = null,
	val address: String? = null,
	val active_permission: List<TronAccountPermission>? = null,
	val votes: List<TronVote>? = null,
	val frozenV2: List<TronFrozen>? = null,
	val unfrozenV2: List<TronUnfrozen>? = null
)

@Serializable
data class TronAccountRequest (
	val address: String,
	val visible: Boolean
)

@Serializable
data class TronAccountUsage (
	val freeNetUsed: Int? = null,
	val freeNetLimit: Int? = null
)

@Serializable
data class TronEmptyAccount (
	val address: String? = null
)

@Serializable
data class TronReward (
	val reward: Long? = null
)

@Serializable
data class WitnessAccount (
	val address: String,
	val voteCount: Long? = null,
	val url: String,
	val isJobs: Boolean? = null
)

@Serializable
data class WitnessesList (
	val witnesses: List<WitnessAccount>
)

