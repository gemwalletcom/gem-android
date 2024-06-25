/**
 * Generated by typeshare 1.7.0
 */

@file:NoLiveLiterals

package com.wallet.core.blockchain.bnbchain.models

import androidx.compose.runtime.NoLiveLiterals
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BNBChainMessageFee (
	val msg_type: String,
	val fee: UInt
)

@Serializable
data class BNBChainFixedFee (
	val fixed_fee_params: BNBChainMessageFee
)

@Serializable
enum class BNBChainMessageFeeType(val string: String) {
	@SerialName("send")
	send("send"),
}

