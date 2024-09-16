/**
 * Generated by typeshare 1.11.0
 */

package com.wallet.core.primitives

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ConfigAppVersion (
	val production: String,
	val beta: String,
	val alpha: String
)

@Serializable
data class ConfigAndroidApp (
	val version: ConfigAppVersion
)

@Serializable
data class ConfigIOSApp (
	val version: ConfigAppVersion
)

@Serializable
data class ConfigApp (
	val ios: ConfigIOSApp,
	val android: ConfigAndroidApp
)

@Serializable
data class ConfigVersions (
	val fiatAssets: Int,
	val swapAssets: Int
)

@Serializable
data class ConfigResponse (
	val app: ConfigApp,
	val versions: ConfigVersions
)

