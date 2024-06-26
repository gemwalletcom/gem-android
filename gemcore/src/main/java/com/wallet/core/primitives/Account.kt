/**
 * Generated by typeshare 1.9.2
 */

@file:NoLiveLiterals

package com.wallet.core.primitives

import androidx.compose.runtime.NoLiveLiterals
import kotlinx.serialization.Serializable

@Serializable
data class Account (
	val chain: Chain,
	val address: String,
	val derivationPath: String,
	val extendedPublicKey: String? = null
)

