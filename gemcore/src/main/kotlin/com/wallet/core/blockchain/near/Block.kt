/**
 * Generated by typeshare 1.13.2
 */

package com.wallet.core.blockchain.near

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class NearBlockHeader (
	val hash: String,
	val height: Long
)

@Serializable
data class NearBlock (
	val header: NearBlockHeader
)

@Serializable
data class NearGenesisConfig (
	val chain_id: String
)

