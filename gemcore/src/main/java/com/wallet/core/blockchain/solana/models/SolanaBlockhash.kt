/**
 * Generated by typeshare 1.9.2
 */

@file:NoLiveLiterals

package com.wallet.core.blockchain.solana.models

import androidx.compose.runtime.NoLiveLiterals
import kotlinx.serialization.Serializable

@Serializable
data class SolanaBlockhash (
	val blockhash: String
)

@Serializable
data class SolanaBlockhashResult (
	val value: SolanaBlockhash
)

