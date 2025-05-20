package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.clients.tron.services.TronCallService
import com.gemwallet.android.blockchain.clients.tron.services.TronNodeStatusService
import com.gemwallet.android.blockchain.clients.tron.services.staked
import com.gemwallet.android.blockchain.clients.tron.services.triggerSmartContract
import com.gemwallet.android.ext.type
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.wallet.core.blockchain.tron.models.TronAccount
import com.wallet.core.blockchain.tron.models.TronAccountUsage
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import wallet.core.jni.Base58
import java.math.BigDecimal
import java.math.BigInteger

class TronFeeCalculator(
    private val chain: Chain,
    private val nodeStatusService: TronNodeStatusService,
    private val callService: TronCallService,
) {
    val baseFee = BigInteger.valueOf(280_000)

    suspend fun calculate(inParams: ConfirmParams.Stake, account: TronAccount?, accountUsage: TronAccountUsage?) = withContext(Dispatchers.IO) {
        if (accountUsage == null || account == null) {
            throw Exception("Account doesn't available (empty balance?)")
        }

        val availableBandwidth = (accountUsage.freeNetLimit ?: 0) - (accountUsage.freeNetUsed ?: 0)
        val fee = when (inParams) {
            is ConfirmParams.Stake.DelegateParams -> if (availableBandwidth >= 580) BigInteger.ZERO else baseFee
            is ConfirmParams.Stake.UndelegateParams -> if (account.staked(chain) > inParams.amount) {
                if (availableBandwidth >= 580) BigInteger.ZERO else baseFee * BigInteger.valueOf(2)
            } else {
                if (availableBandwidth >= 300) BigInteger.ZERO else baseFee
            }
            is ConfirmParams.Stake.RedelegateParams,
            is ConfirmParams.Stake.RewardsParams,
            is ConfirmParams.Stake.WithdrawParams -> if (availableBandwidth >= 300) BigInteger.ZERO else baseFee
        }
        Fee(FeePriority.Normal, AssetId(chain), fee)
    }

    suspend fun calculate(inParams: ConfirmParams.TransferParams, account: TronAccount?, accountUsage: TronAccountUsage?) = withContext(Dispatchers.IO) {
        val getParams = async { nodeStatusService.getChainParameters().fold({ it.chainParameter }) { null } }

        val isNewAccount = account?.address.isNullOrEmpty()
        val params = getParams.await()
        val newAccountFeeInSmartContract = params?.firstOrNull { it.key == "getCreateNewAccountFeeInSystemContract" }?.value
        val newAccountFee = params?.firstOrNull{ it.key == "getCreateAccountFee" }?.value
        val energyFee = params?.firstOrNull { it.key == "getEnergyFee" }?.value

        if (newAccountFeeInSmartContract == null || newAccountFee == null || energyFee == null) {
            throw Exception("Tron unknown key")
        }

        val fee = when (inParams.assetId.type()) {
            AssetSubtype.NATIVE -> {
                val availableBandwidth = (accountUsage?.freeNetLimit ?: 0) - (accountUsage?.freeNetUsed ?: 0)
                val coinTransferFee = if (availableBandwidth >= 300) BigInteger.ZERO else baseFee
                if (isNewAccount) coinTransferFee + BigInteger.valueOf(newAccountFee) else coinTransferFee
            }
            else -> {
                // https://developers.tron.network/docs/set-feelimit#how-to-estimate-energy-consumption
                val gasLimit = estimateTRC20Transfer(
                    ownerAddress = inParams.from.address,
                    recipientAddress =  inParams.destination.address,
                    contractAddress = inParams.assetId.tokenId ?: throw IllegalArgumentException("Incorrect contract on fee calculation"),
                    value = inParams.amount,
                )
                val tokenTransfer = BigInteger.valueOf(energyFee) * gasLimit.add(gasLimit.multiply(BigDecimal("0.2"))).toBigInteger()
                if (isNewAccount) tokenTransfer + BigInteger.valueOf(newAccountFeeInSmartContract) else tokenTransfer
            }
        }
        Fee(FeePriority.Normal, AssetId(chain), fee)
    }

    // https://developers.tron.network/docs/set-feelimit#how-to-estimate-energy-consumption
    private suspend fun estimateTRC20Transfer(
        ownerAddress: String,
        recipientAddress: String,
        contractAddress: String,
        value: BigInteger
    ): BigDecimal {
        val address = Base58.decode(recipientAddress).toHexString("")
        val parameter = arrayOf(
            address,
            value.toByteArray().toHexString("")
        ).joinToString(separator = "") { it.padStart(64, '0') }
        val response = callService.triggerSmartContract(
            contractAddress = contractAddress,
            functionSelector = "transfer(address,uint256)",
            parameter = parameter,
            feeLimit = 0L,
            callValue = 0L,
            ownerAddress = ownerAddress,
            visible = true
        )
        val result = response.getOrNull()
        if (result == null || !result.result.message.isNullOrEmpty()) {
            throw IllegalStateException("Can't get gas limit")
        }
        return BigDecimal.valueOf(result.energy_used.toLong())
    }
}