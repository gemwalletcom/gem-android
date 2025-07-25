/**
 * Generated by typeshare 1.13.2
 */

package com.wallet.core.blockchain.solana

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class SolanaEpoch (
	val epoch: Long,
	val slotIndex: Long,
	val slotsInEpoch: Long
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

