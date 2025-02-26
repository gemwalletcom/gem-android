/**
 * Generated by typeshare 1.12.0
 */

package com.wallet.core.blockchain.cosmos.models

import kotlinx.serialization.Serializable

@Serializable
data class CosmosHeader (
	val chain_id: String,
	val height: String
)

@Serializable
data class CosmosBlock (
	val header: CosmosHeader
)

@Serializable
data class CosmosBlockResponse (
	val block: CosmosBlock
)

@Serializable
data class CosmosNodeInfo (
	val network: String
)

@Serializable
data class CosmosNodeInfoResponse (
	val default_node_info: CosmosNodeInfo
)

