/**
 * Generated by typeshare 1.13.2
 */

package com.wallet.core.primitives

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
enum class TransactionState(val string: String) {
	@SerialName("pending")
	Pending("pending"),
	@SerialName("confirmed")
	Confirmed("confirmed"),
	@SerialName("failed")
	Failed("failed"),
	@SerialName("reverted")
	Reverted("reverted"),
}

