/**
 * Generated by typeshare 1.13.2
 */

package com.wallet.core.primitives

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class EIP712Domain (
	val name: String,
	val version: String,
	val chainId: UInt,
	val verifyingContract: String
)

@Serializable
data class EIP712Type (
	val name: String,
	val type: String
)

