/**
 * Generated by typeshare 1.13.2
 */

package com.wallet.core.primitives

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Device (
	val id: String,
	val platform: Platform,
	val platformStore: PlatformStore? = null,
	val token: String,
	val locale: String,
	val version: String,
	val currency: String,
	val isPushEnabled: Boolean,
	val isPriceAlertsEnabled: Boolean? = null,
	val subscriptionsVersion: Int
)

