/**
 * Generated by typeshare 1.13.2
 */

package com.wallet.core.blockchain.ton.models

import kotlinx.serialization.Serializable

@Serializable
data class TonEstimateFee (
	val address: String,
	val body: String,
	val ignore_chksig: Boolean
)

@Serializable
data class TonFee (
	val in_fwd_fee: Int,
	val storage_fee: Int
)

@Serializable
data class TonFees (
	val source_fees: TonFee
)

