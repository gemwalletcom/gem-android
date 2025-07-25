/**
 * Generated by typeshare 1.13.2
 */

package com.wallet.core.blockchain.ton

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class TonBlock (
	val seqno: Int,
	val root_hash: String
)

@Serializable
data class TonMasterchainInfo (
	val last: TonBlock
)

