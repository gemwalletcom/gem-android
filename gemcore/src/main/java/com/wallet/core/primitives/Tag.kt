/**
 * Generated by typeshare 1.13.2
 */

package com.wallet.core.primitives

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
enum class AssetTag(val string: String) {
	@SerialName("trending")
	Trending("trending"),
	@SerialName("gainers")
	Gainers("gainers"),
	@SerialName("losers")
	Losers("losers"),
	@SerialName("new")
	New("new"),
	@SerialName("stablecoins")
	Stablecoins("stablecoins"),
}

