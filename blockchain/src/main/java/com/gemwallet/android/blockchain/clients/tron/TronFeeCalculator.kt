package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.ext.type
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import wallet.core.jni.Base58
import java.math.BigDecimal
import java.math.BigInteger

class TronFeeCalculator(
    private val chain: Chain,
    private val rpcClient: TronRpcClient,

) {
    suspend fun calculate(inParams: ConfirmParams.TransferParams) = withContext(Dispatchers.IO) {
        val getIsNewAccount = async { rpcClient.getAccount(inParams.from.address) }
        val getAccountUsage = async { rpcClient.getAccountUsage(inParams.from.address) }
        val getParams = async { rpcClient.getChainParameters().fold({ it.chainParameter }) { null } }

        val isNewAccount = getIsNewAccount.await()
        val params = getParams.await()
        val accountUsage = getAccountUsage.await()

        val newAccountFeeInSmartContract = params?.firstOrNull { it.key == "getCreateNewAccountFeeInSystemContract" }?.value
        val newAccountFee = params?.firstOrNull{ it.key == "getCreateAccountFee" }?.value
        val energyFee = params?.firstOrNull { it.key == "getEnergyFee" }?.value

        if (newAccountFeeInSmartContract == null || newAccountFee == null || energyFee == null) {
            throw Exception("Tron unknown key")
        }

        val fee = when (inParams.assetId.type()) {
            AssetSubtype.NATIVE -> {
                val availableBandwidth = accountUsage?.freeNetLimit ?: (0 - (accountUsage?.freeNetUsed ?: 0))
                val coinTransferFee = if (availableBandwidth >= 300) BigInteger.ZERO else BigInteger.valueOf(280_000)
                if (isNewAccount) coinTransferFee + BigInteger.valueOf(newAccountFee) else coinTransferFee
            }
            else -> {
                // https://developers.tron.network/docs/set-feelimit#how-to-estimate-energy-consumption
                val gasLimit = estimateTRC20Transfer(
                    rpcClient = rpcClient,
                    ownerAddress = inParams.from.address,
                    recipientAddress =  inParams.destination.address,
                    contractAddress = inParams.assetId.tokenId ?: throw IllegalArgumentException("Incorrect contract on fee calculation"),
                    value = inParams.amount,
                )
                val tokenTransfer = BigInteger.valueOf(energyFee) * gasLimit.add(gasLimit.multiply(BigDecimal("0.2"))).toBigInteger()
                if (isNewAccount) tokenTransfer + BigInteger.valueOf(newAccountFeeInSmartContract) else tokenTransfer
            }
        }
        Fee(TxSpeed.Normal, AssetId(chain), fee)
    }

    // https://developers.tron.network/docs/set-feelimit#how-to-estimate-energy-consumption
    private suspend fun estimateTRC20Transfer(
        rpcClient: TronRpcClient,
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
        val result = rpcClient.triggerSmartContract(
            contractAddress = contractAddress,
            functionSelector = "transfer(address,uint256)",
            parameter = parameter,
            feeLimit = 0L,
            callValue = 0L,
            ownerAddress = ownerAddress,
            visible = true
        ).getOrNull()
        if (result == null || !result.result.message.isNullOrEmpty()) {
            throw IllegalStateException("Can't get gas limit")
        }
        return BigDecimal.valueOf(result.energy_used.toLong())
    }
}