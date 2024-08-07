/**
 * Generated by typeshare 1.9.2
 */

@file:NoLiveLiterals

package com.wallet.core.primitives

import androidx.compose.runtime.NoLiveLiterals
import kotlinx.serialization.*

@Serializable
enum class EncodingType(val string: String) {
	@SerialName("Hex")
	Hex("Hex"),
	@SerialName("Base58")
	Base58("Base58"),
}

