/**
 * Generated by typeshare 1.13.2
 */

package com.wallet.core.primitives

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
enum class BalanceType(val string: String) {
	@SerialName("available")
	available("available"),
	@SerialName("locked")
	locked("locked"),
	@SerialName("frozen")
	frozen("frozen"),
	@SerialName("staked")
	staked("staked"),
	@SerialName("pending")
	pending("pending"),
	@SerialName("rewards")
	rewards("rewards"),
	@SerialName("reserved")
	reserved("reserved"),
}

