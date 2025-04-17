/**
 * Generated by typeshare 1.13.2
 */

package com.wallet.core.primitives

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
enum class EVMChain(val string: String) {
	@SerialName("ethereum")
	Ethereum("ethereum"),
	@SerialName("smartchain")
	SmartChain("smartchain"),
	@SerialName("polygon")
	Polygon("polygon"),
	@SerialName("arbitrum")
	Arbitrum("arbitrum"),
	@SerialName("optimism")
	Optimism("optimism"),
	@SerialName("base")
	Base("base"),
	@SerialName("avalanchec")
	AvalancheC("avalanchec"),
	@SerialName("opbnb")
	OpBNB("opbnb"),
	@SerialName("fantom")
	Fantom("fantom"),
	@SerialName("gnosis")
	Gnosis("gnosis"),
	@SerialName("manta")
	Manta("manta"),
	@SerialName("blast")
	Blast("blast"),
	@SerialName("zksync")
	ZkSync("zksync"),
	@SerialName("linea")
	Linea("linea"),
	@SerialName("mantle")
	Mantle("mantle"),
	@SerialName("celo")
	Celo("celo"),
	@SerialName("world")
	World("world"),
	@SerialName("sonic")
	Sonic("sonic"),
	@SerialName("abstract")
	Abstract("abstract"),
	@SerialName("berachain")
	Berachain("berachain"),
	@SerialName("ink")
	Ink("ink"),
	@SerialName("unichain")
	Unichain("unichain"),
	@SerialName("hyperliquid")
	Hyperliquid("hyperliquid"),
	@SerialName("monad")
	Monad("monad"),
	@SerialName("xdc")
	XDC("xdc"),
}

