package com.gemwallet.android.blockchain.clients.polkadot

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.polkadot.models.PolkadotSigningData
import com.gemwallet.android.blockchain.clients.polkadot.services.PolkadotBalancesService
import com.gemwallet.android.blockchain.clients.polkadot.services.PolkadotFeeService
import com.gemwallet.android.blockchain.clients.polkadot.services.PolkadotTransactionService
import com.gemwallet.android.ext.asset
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.blockchain.polkadot.PolkadotTransactionPayload
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class PolkadotSignerPreloaderClient(
    private val chain: Chain,
    private val transactionService: PolkadotTransactionService,
    private val feeService: PolkadotFeeService,
    private val balancesService: PolkadotBalancesService,
) : NativeTransferPreloader {

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams = withContext(Dispatchers.IO) {
        val getTransactionMaterial = async { transactionService.transactionMaterial().getOrNull() }
        val getBalance = async { balancesService.balance(params.destination.address).getOrNull() }
        val getNonce = async { balancesService.balance(params.from.address).getOrNull()?.nonce?.toLong() }

        val transactionMaterial = getTransactionMaterial.await() ?: throw Exception("Can't load chain data")
        val balance = getBalance.await() ?: throw Exception("Can't load chain data")
        val nonce = getNonce.await() ?: throw Exception("Can't load nonce")

//        TODO: Check reserved 1DOT
//        if (Crypto(balance.reserved).value(chain.asset().decimals) < BigDecimal.ONE) {
//            throw IllegalStateException("Doesn't active recipient")
//        }

        val signData = PolkadotSigningData(
            genesisHash = transactionMaterial.genesisHash,
            blockHash = transactionMaterial.at.hash,
            blockNumber = transactionMaterial.at.height.toBigInteger(),
            specVersion = transactionMaterial.specVersion.toBigInteger(),
            transactionVersion = transactionMaterial.txVersion.toBigInteger(),
            period = periodLength
        )

        val transactionData = PolkadotSignClient.transactionPayload(
            params.destination.address,
            params.amount,
            nonce = nonce,
            data = signData,
        )
        val feeAmount = feeService.fee(PolkadotTransactionPayload(transactionData)).getOrNull()?.partialFee?.toBigInteger()
            ?: throw java.lang.Exception("Can't estimate fee")
        val fee = Fee(
            speed = TxSpeed.Normal,
            feeAssetId = AssetId(chain),
            amount = feeAmount
        )
        SignerParams(
            input = params,
            chainData = PolkadotChainData(
                sequence = nonce.toInt(),
                data = signData,
                blockNumber = signData.blockNumber.toInt(),
                fee = fee
            )
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    data class PolkadotChainData(
        val sequence: Int,
        val data: PolkadotSigningData,
        val blockNumber: Int,
        val fee: Fee
    ) : ChainSignData {
        override fun fee(speed: TxSpeed): Fee = fee

        override fun blockNumber(): String = blockNumber.toString()
    }

    companion object {
        const val periodLength: Long = 64
    }
}