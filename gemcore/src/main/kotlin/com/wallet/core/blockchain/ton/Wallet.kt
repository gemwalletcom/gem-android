/**
 * Generated by typeshare 1.13.2
 */

package com.wallet.core.blockchain.ton

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class TonWalletInfo (
	val seqno: Int? = null,
	val last_transaction_id: TonTransactionId
)

