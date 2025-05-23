/**
 * Generated by typeshare 1.13.2
 */

package com.wallet.core.primitives

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class AssetPriceInfo (
	val assetId: AssetId,
	val price: Price,
	val market: AssetMarket
)

