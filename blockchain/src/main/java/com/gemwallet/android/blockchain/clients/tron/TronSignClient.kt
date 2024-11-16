package com.gemwallet.android.blockchain.clients.tron

import android.text.format.DateUtils
import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.ext.type
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.google.protobuf.ByteString
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import wallet.core.java.AnySigner
import wallet.core.jni.CoinType
import wallet.core.jni.proto.Tron
import java.math.BigInteger

class TronSignClient(
    private val chain: Chain,
) : SignClient {
    override suspend fun signTransfer(
        params: SignerParams,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): ByteArray {
        val blockInfo = params.info as TronSignerPreloader.Info
        val transaction = Tron.Transaction.newBuilder().apply {
            this.blockHeader = Tron.BlockHeader.newBuilder().apply {
                this.number = blockInfo.number
                this.parentHash = ByteString.copyFrom(blockInfo.parentHash.decodeHex())
                this.timestamp = blockInfo.timestamp
                this.version = blockInfo.version.toInt()
                this.witnessAddress = ByteString.copyFrom(blockInfo.witnessAddress.decodeHex())
                this.txTrieRoot = ByteString.copyFrom(blockInfo.txTrieRoot.decodeHex())
            }.build()
            when (params.input.assetId.type()) {
                AssetSubtype.NATIVE -> this.transfer = getTransferContract(params.finalAmount, params.owner, params.input.destination()?.address ?: "")
                AssetSubtype.TOKEN -> this.transferTrc20Contract = getTransferTRC20Contract(
                    params.input.assetId.tokenId!!,
                    params.finalAmount,
                    params.owner,
                    params.input.destination()?.address ?: ""
                )
                else -> throw IllegalArgumentException("Unsupported type")
            }
            this.expiration = blockInfo.timestamp + 10 * DateUtils.HOUR_IN_MILLIS
            this.timestamp = blockInfo.timestamp
            this.feeLimit = blockInfo.fee().amount.toLong()
        }
        val signInput = Tron.SigningInput.newBuilder().apply {
            this.transaction = transaction.build()
            this.privateKey = ByteString.copyFrom(privateKey)
        }.build()
        val signingOutput = AnySigner.sign(signInput, CoinType.TRON, Tron.SigningOutput.parser())
        return signingOutput.json.toByteArray()
    }

    private fun getTransferContract(value: BigInteger, ownerAddress: String, recipient: String)
        = Tron.TransferContract.newBuilder().apply {
            this.amount = value.toLong()
            this.ownerAddress = ownerAddress
            this.toAddress = recipient
        }.build()

    private fun getTransferTRC20Contract(tokenId: String, value: BigInteger, ownerAddress: String, recipient: String)
            = Tron.TransferTRC20Contract.newBuilder().apply {
        this.contractAddress = tokenId
        this.ownerAddress = ownerAddress
        this.toAddress = recipient
        this.amount = ByteString.copyFrom(value.toByteArray())
    }.build()

    override fun isMaintain(chain: Chain): Boolean = this.chain == chain
}