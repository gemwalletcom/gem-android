/**
 * Generated by typeshare 1.9.2
 */

@file:NoLiveLiterals

package com.wallet.core.blockchain.tron.models

import androidx.compose.runtime.NoLiveLiterals
import kotlinx.serialization.*

@Serializable
data class TronAccountRequest (
	val address: String,
	val visible: Boolean
)

@Serializable
data class TronAccount (
	val balance: Long? = null,
	val address: String? = null
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

