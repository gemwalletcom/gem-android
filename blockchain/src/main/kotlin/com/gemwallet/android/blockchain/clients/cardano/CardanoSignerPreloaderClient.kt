package com.gemwallet.android.blockchain.clients.cardano

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.cardano.services.CardanoServices
import com.gemwallet.android.blockchain.clients.cardano.services.utxos
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerParams
import com.google.protobuf.ByteString
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority
import com.wallet.core.primitives.UTXO
import wallet.core.java.AnySigner
import wallet.core.jni.CoinType
import wallet.core.jni.proto.Cardano

class CardanoSignerPreloaderClient(
    private val chain: Chain,
    private val feeServices: CardanoServices
) : NativeTransferPreloader {

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams {
        val utxos = feeServices.utxos(params.from.address)
        val fee = calculateFee(params, utxos)

        return SignerParams(
            input = params,
            chainData = CardanoChainData(
                fee = Fee(
                    feeAssetId = AssetId(chain),
                    priority = FeePriority.Normal,
                    amount = fee.toBigInteger(),
                ),
                utxos = utxos,
            )
        )
    }


    override fun supported(chain: Chain): Boolean = this.chain == chain

    private fun calculateFee(params: ConfirmParams.TransferParams.Native, utxos: List<UTXO>): Long {
        val signingInput = Cardano.SigningInput.newBuilder().apply {
            this.addAllUtxos(
                utxos.map { utxo ->
                    Cardano.TxInput.newBuilder().apply {
                        this.address = utxo.address
                        this.amount = utxo.value.toLong()
                        this.outPoint = Cardano.OutPoint.newBuilder().apply {
                            this.txHash = ByteString.copyFrom(utxo.transaction_id.decodeHex())
                            this.outputIndex = utxo.vout.toLong()
                        }.build()
                    }.build()
                }
            )
            this.transferMessage = Cardano.Transfer.newBuilder().apply {
                this.toAddress = params.destination.address
                this.changeAddress = params.from.address
                this.amount = params.amount.toLong()
                this.useMaxAmount = params.isMaxAmount
            }.build()
        }.build()
        val plan: Cardano.TransactionPlan = AnySigner.plan(signingInput, CoinType.CARDANO, Cardano.TransactionPlan.parser())
        return plan.fee
    }

    data class CardanoChainData(
        val fee: Fee,
        val utxos: List<UTXO>,
    ) : ChainSignData {

        override fun fee(speed: FeePriority): Fee = fee

    }
}