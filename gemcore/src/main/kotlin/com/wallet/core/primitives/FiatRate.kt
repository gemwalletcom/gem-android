/**
 * Generated by typeshare 1.13.2
 */

package com.wallet.core.primitives

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class FiatRate (
	val symbol: String,
	val rate: Double
)

