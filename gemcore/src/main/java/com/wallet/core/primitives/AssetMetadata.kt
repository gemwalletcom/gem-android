/**
 * Generated by typeshare 1.13.2
 */

package com.wallet.core.primitives

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class AssetMetaData (
	val isEnabled: Boolean,
	val isBuyEnabled: Boolean,
	val isSellEnabled: Boolean,
	val isSwapEnabled: Boolean,
	val isStakeEnabled: Boolean,
	val isPinned: Boolean,
	val isActive: Boolean,
	val stakingApr: Double? = null
)

