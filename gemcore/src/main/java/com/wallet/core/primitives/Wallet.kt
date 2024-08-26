/**
 * Generated by typeshare 1.9.2
 */

@file:NoLiveLiterals

package com.wallet.core.primitives

import androidx.compose.runtime.NoLiveLiterals
import kotlinx.serialization.*

@Serializable
enum class WalletType(val string: String) {
	@SerialName("multicoin")
	multicoin("multicoin"),
	@SerialName("single")
	single("single"),
	@SerialName("privateKey")
	private_key("privateKey"),
	@SerialName("view")
	view("view"),
}

@Serializable
data class Wallet (
	val id: String,
	val name: String,
	val index: Int,
	val type: WalletType,
	val accounts: List<Account>
)

@Serializable
data class WalletId (
	val id: String
)

