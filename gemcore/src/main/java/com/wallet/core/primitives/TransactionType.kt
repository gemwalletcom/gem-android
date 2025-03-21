/**
 * Generated by typeshare 1.13.2
 */

package com.wallet.core.primitives

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
enum class TransactionType(val string: String) {
	@SerialName("transfer")
	Transfer("transfer"),
	@SerialName("transferNFT")
	TransferNFT("transferNFT"),
	@SerialName("swap")
	Swap("swap"),
	@SerialName("tokenApproval")
	TokenApproval("tokenApproval"),
	@SerialName("stakeDelegate")
	StakeDelegate("stakeDelegate"),
	@SerialName("stakeUndelegate")
	StakeUndelegate("stakeUndelegate"),
	@SerialName("stakeRewards")
	StakeRewards("stakeRewards"),
	@SerialName("stakeRedelegate")
	StakeRedelegate("stakeRedelegate"),
	@SerialName("stakeWithdraw")
	StakeWithdraw("stakeWithdraw"),
	@SerialName("assetActivation")
	AssetActivation("assetActivation"),
	@SerialName("smartContractCall")
	SmartContractCall("smartContractCall"),
}

