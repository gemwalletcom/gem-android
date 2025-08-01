/**
 * Generated by typeshare 1.13.2
 */

package com.wallet.core.blockchain.aptos

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class AptosCoinInfo (
	val decimals: Int,
	val name: String,
	val symbol: String
)

@Serializable
data class AptosLedger (
	val chain_id: Int,
	val ledger_version: String
)

