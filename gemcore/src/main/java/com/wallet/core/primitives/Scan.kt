/**
 * Generated by typeshare 1.9.2
 */

@file:NoLiveLiterals

package com.wallet.core.primitives

import androidx.compose.runtime.NoLiveLiterals
import kotlinx.serialization.Serializable

@Serializable
data class ScanAddress (
	val name: String? = null,
	val address: String,
	val isVerified: Boolean,
	val isFradulent: Boolean,
	val isMemoRequired: Boolean
)

