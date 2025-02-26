/**
 * Generated by typeshare 1.12.0
 */

package com.wallet.core.blockchain.ton.models

import kotlinx.serialization.Serializable

@Serializable
data class TonResult<T> (
	val result: T
)

@Serializable
data class TonWalletInfo (
	val seqno: Int? = null,
	val last_transaction_id: TonTransactionId
)

