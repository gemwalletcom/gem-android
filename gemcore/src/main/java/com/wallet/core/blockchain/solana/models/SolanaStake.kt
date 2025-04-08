/**
 * Generated by typeshare 1.13.2
 */

package com.wallet.core.blockchain.solana.models

import kotlinx.serialization.Serializable

@Serializable
data class SolanaEpoch (
	val epoch: Int,
	val slotIndex: Int,
	val slotsInEpoch: Int
)

@Serializable
data class SolanaValidator (
	val votePubkey: String,
	val commission: Int,
	val epochVoteAccount: Boolean
)

@Serializable
data class SolanaValidators (
	val current: List<SolanaValidator>
)

