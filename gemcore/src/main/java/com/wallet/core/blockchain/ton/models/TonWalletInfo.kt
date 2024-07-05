/**
 * Generated by typeshare 1.9.2
 */

@file:NoLiveLiterals

package com.wallet.core.blockchain.ton.models

import androidx.compose.runtime.NoLiveLiterals
import kotlinx.serialization.*

@Serializable
data class TonWalletInfo (
	val seqno: Int? = null,
	val last_transaction_id: TonTransactionId
)

@Serializable
data class TonResult<T> (
	val result: T
)

