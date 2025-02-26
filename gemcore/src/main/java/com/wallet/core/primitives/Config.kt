/**
 * Generated by typeshare 1.12.0
 */

package com.wallet.core.primitives

import kotlinx.serialization.Serializable

@Serializable
data class Release (
	val version: String,
	val store: PlatformStore,
	val upgradeRequired: Boolean
)

@Serializable
data class ConfigVersions (
	val fiatOnRampAssets: Int,
	val fiatOffRampAssets: Int,
	val swapAssets: Int
)

@Serializable
data class ConfigResponse (
	val releases: List<Release>,
	val versions: ConfigVersions
)

