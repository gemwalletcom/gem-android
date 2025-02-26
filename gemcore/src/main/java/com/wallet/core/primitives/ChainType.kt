/**
 * Generated by typeshare 1.12.0
 */

package com.wallet.core.primitives

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ChainType(val string: String) {
	@SerialName("ethereum")
	Ethereum("ethereum"),
	@SerialName("bitcoin")
	Bitcoin("bitcoin"),
	@SerialName("solana")
	Solana("solana"),
	@SerialName("cosmos")
	Cosmos("cosmos"),
	@SerialName("ton")
	Ton("ton"),
	@SerialName("tron")
	Tron("tron"),
	@SerialName("aptos")
	Aptos("aptos"),
	@SerialName("sui")
	Sui("sui"),
	@SerialName("xrp")
	Xrp("xrp"),
	@SerialName("near")
	Near("near"),
	@SerialName("stellar")
	Stellar("stellar"),
	@SerialName("algorand")
	Algorand("algorand"),
	@SerialName("polkadot")
	Polkadot("polkadot"),
	@SerialName("cardano")
	Cardano("cardano"),
}

