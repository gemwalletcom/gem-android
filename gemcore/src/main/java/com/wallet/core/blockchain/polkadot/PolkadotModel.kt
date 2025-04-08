/**
 * Generated by typeshare 1.13.2
 */

package com.wallet.core.blockchain.polkadot

import kotlinx.serialization.Serializable

@Serializable
data class PolkadotAccountBalance (
	val free: String,
	val reserved: String,
	val nonce: String
)

@Serializable
data class PolkadotExtrinsic (
	val hash: String,
	val success: Boolean
)

@Serializable
data class PolkadotBlock (
	val number: String,
	val extrinsics: List<PolkadotExtrinsic>
)

@Serializable
data class PolkadotEstimateFee (
	val partialFee: String
)

@Serializable
data class PolkadotNodeVersion (
	val chain: String
)

@Serializable
data class PolkadotTransactionBroadcast (
	val hash: String
)

@Serializable
data class PolkadotTransactionBroadcastError (
	val error: String,
	val cause: String
)

@Serializable
data class PolkadotTransactionMaterialBlock (
	val height: String,
	val hash: String
)

@Serializable
data class PolkadotTransactionMaterial (
	val at: PolkadotTransactionMaterialBlock,
	val genesisHash: String,
	val chainName: String,
	val specName: String,
	val specVersion: String,
	val txVersion: String
)

@Serializable
data class PolkadotTransactionPayload (
	val tx: String
)

