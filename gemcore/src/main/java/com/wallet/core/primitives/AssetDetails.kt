/**
 * Generated by typeshare 1.12.0
 */

package com.wallet.core.primitives

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class AssetProperties (
	val isBuyable: Boolean,
	val isSellable: Boolean,
	val isSwapable: Boolean,
	val isStakeable: Boolean,
	val stakingApr: Double? = null
)

@Serializable
data class AssetBasic (
	val asset: Asset,
	val properties: AssetProperties,
	val score: AssetScore
)

@Serializable
data class AssetLinks (
	val homepage: String? = null,
	val explorer: String? = null,
	val twitter: String? = null,
	val telegram: String? = null,
	val github: String? = null,
	val youtube: String? = null,
	val facebook: String? = null,
	val reddit: String? = null,
	val coingecko: String? = null,
	val coinmarketcap: String? = null,
	val discord: String? = null
)

@Serializable
data class AssetDetails (
	val links: AssetLinks,
	val isBuyable: Boolean,
	val isSellable: Boolean,
	val isSwapable: Boolean,
	val isStakeable: Boolean,
	val stakingApr: Double? = null
)

@Serializable
data class AssetDetailsInfo (
	val details: AssetDetails,
	val market: AssetMarket
)

@Serializable
data class AssetLink (
	val name: String,
	val url: String
)

@Serializable
data class AssetFull (
	val asset: Asset,
	val links: List<AssetLink>,
	val properties: AssetProperties,
	val score: AssetScore
)

@Serializable
data class AssetMarketPrice (
	val price: Price? = null,
	val market: AssetMarket? = null
)

