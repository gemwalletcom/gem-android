/**
 * Generated by typeshare 1.9.2
 */

@file:NoLiveLiterals

package com.wallet.core.primitives

import androidx.compose.runtime.NoLiveLiterals
import kotlinx.serialization.*

@Serializable
data class AssetId (
	val chain: Chain,
	val tokenId: String? = null
)

