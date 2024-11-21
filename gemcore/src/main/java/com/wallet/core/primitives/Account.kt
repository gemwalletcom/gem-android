/**
 * Generated by typeshare 1.12.0
 */

package com.wallet.core.primitives

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Account (
	val chain: Chain,
	val address: String,
	val derivationPath: String,
	val extendedPublicKey: String? = null
)

