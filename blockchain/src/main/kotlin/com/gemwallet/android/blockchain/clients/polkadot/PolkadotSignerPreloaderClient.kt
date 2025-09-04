package com.gemwallet.android.blockchain.clients.polkadot

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.polkadot.models.PolkadotSigningData
import com.gemwallet.android.blockchain.clients.polkadot.services.PolkadotServices
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerParams
import com.wallet.core.blockchain.polkadot.PolkadotTransactionPayload
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class PolkadotSignerPreloaderClient(
    private val chain: Chain,
    private val client: PolkadotServices,
) : NativeTransferPreloader {

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams = withContext(Dispatchers.IO) {
        val getTransactionMaterial = async { client.transactionMaterial().getOrNull() }
        val getBalance = async { client.balance(params.destination.address).getOrNull() }
        val getNonce = async { client.balance(params.from.address).getOrNull()?.nonce?.toULong() }

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
        val feeAmount = client.fee(PolkadotTransactionPayload(transactionData)).getOrNull()?.partialFee?.toBigInteger()
            ?: throw java.lang.Exception("Can't estimate fee")
        val fee = Fee(
            priority = FeePriority.Normal,
            feeAssetId = AssetId(chain),
            amount = feeAmount
        )
        SignerParams(
            input = params,
            chainData = PolkadotChainData(
                sequence = nonce,
                genesisHash = transactionMaterial.genesisHash,
                blockHash = transactionMaterial.at.hash,
                blockNumber = transactionMaterial.at.height.toULong(),
                specVersion = transactionMaterial.specVersion.toULong(),
                transactionVersion = transactionMaterial.txVersion.toULong(),
                period = periodLength
            ),
            fee = listOf(fee)
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    companion object {
        const val periodLength: Long = 64
    }
}