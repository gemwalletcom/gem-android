/**
 * Generated by typeshare 1.11.0
 */

package com.wallet.core.primitives

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
enum class Platform(val string: String) {
	@SerialName("ios")
	IOS("ios"),
	@SerialName("android")
	Android("android"),
}

