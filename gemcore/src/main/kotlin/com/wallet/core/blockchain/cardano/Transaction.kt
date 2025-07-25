/**
 * Generated by typeshare 1.13.2
 */

package com.wallet.core.blockchain.cardano

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class CardanoSubmitTransactionHash (
	val hash: String
)

@Serializable
data class CardanoTransaction (
	val fee: String,
	val block: CardanoBlock
)

@Serializable
data class CardanoTransactionBroadcast (
	val submitTransaction: CardanoSubmitTransactionHash? = null
)

@Serializable
data class CardanoTransactions (
	val transactions: List<CardanoTransaction>
)

