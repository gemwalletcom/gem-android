package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.operators.GetAsset
import com.gemwallet.android.ext.type
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.SignerParams
import com.google.protobuf.ByteString
import com.google.protobuf.MessageLite
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType
import wallet.core.java.AnySigner
import wallet.core.jni.Base58
import wallet.core.jni.Base64
import wallet.core.jni.CoinType
import wallet.core.jni.Curve
import wallet.core.jni.PrivateKey
import wallet.core.jni.SolanaAddress
import wallet.core.jni.proto.Solana

class SolanaSignClient(
    private val getAsset: GetAsset,
) : SignClient {

    override suspend fun signMessage(input: ByteArray, privateKey: ByteArray): ByteArray {
        val str = String(input)
        val bytes = Base64.decode(str)
        if (bytes[0].toInt() != 1) {
            throw IllegalArgumentException("only support one signature")
        }

        val message = bytes.copyOfRange(65, bytes.size - 1)
        val signature = PrivateKey(privateKey).sign(message, Curve.ED25519)
        val signed = byteArrayOf(0x1) + signature + message
        return Base64.encode(signed).toByteArray()
    }

    override suspend fun signTransfer(
        params: SignerParams,
        privateKey: ByteArray
    ): ByteArray {
        val recentBlockhash = (params.info as SolanaSignerPreloader.Info).blockhash
        return when(params.input.getTxType()) {
            TransactionType.Swap -> swap(params, privateKey).toByteArray()
            TransactionType.Transfer -> {
                val signInput = when (params.input.assetId.type()) {
                    AssetSubtype.NATIVE -> signNative(params)
                    AssetSubtype.TOKEN -> signToken(params)
                }
                signInput.privateKey = ByteString.copyFrom(privateKey)
                sign(message = signInput.build())
            }
            TransactionType.StakeDelegate -> {
                val signingInput = Solana.SigningInput.newBuilder().apply {
                    this.recentBlockhash = recentBlockhash
                    this.delegateStakeTransaction = Solana.DelegateStake.newBuilder().apply {
                        this.validatorPubkey = (params.input as ConfirmParams.DelegateParams).validatorId
                        this.value = params.finalAmount.toLong()
                    }.build()
                    this.privateKey = ByteString.copyFrom(privateKey)
                }
                sign(message = signingInput.build())
            }
            TransactionType.StakeUndelegate -> {
                val signingInput = Solana.SigningInput.newBuilder().apply {
                    this.recentBlockhash = recentBlockhash
                    this.deactivateStakeTransaction = Solana.DeactivateStake.newBuilder().apply {
                        this.stakeAccount = (params.input as ConfirmParams.UndelegateParams).delegationId
                    }.build()
                    this.privateKey = ByteString.copyFrom(privateKey)
                }
                sign(message = signingInput.build())
            }
            TransactionType.StakeWithdraw -> {
                val signingInput = Solana.SigningInput.newBuilder().apply {
                    this.recentBlockhash = recentBlockhash
                    this.withdrawTransaction = Solana.WithdrawStake.newBuilder().apply {
                        stakeAccount = (params.input as ConfirmParams.WithdrawParams).delegationId
                        value = params.finalAmount.toLong()
                    }.build()
                    this.privateKey = ByteString.copyFrom(privateKey)
                }
                sign(message = signingInput.build())
            }
            TransactionType.TokenApproval,
            TransactionType.StakeRedelegate,
            TransactionType.StakeRewards -> throw IllegalArgumentException()
        }
    }

    private fun signNative(input: SignerParams): Solana.SigningInput.Builder {
        val blockhash = (input.info as SolanaSignerPreloader.Info).blockhash
        return Solana.SigningInput.newBuilder().apply {
            this.transferTransaction = Solana.Transfer.newBuilder().apply {
                this.recipient = input.input.destination()
                this.value = input.finalAmount.toLong()
                if (!input.input.memo().isNullOrEmpty()) {
                    this.memo = input.input.memo()
                }
            }.build()
            this.recentBlockhash = blockhash
        }
    }

    private suspend fun signToken(input: SignerParams): Solana.SigningInput.Builder {
        val decimals = getAsset.getAsset(input.input.assetId)?.decimals ?: throw IllegalArgumentException("Asset not found")
        val tokenId = input.input.assetId.tokenId
        val amount = input.finalAmount.toLong()
        val recipient = input.input.destination()
        val metadata = input.info as SolanaSignerPreloader.Info
        return Solana.SigningInput.newBuilder().apply {
            this.recentBlockhash = metadata.blockhash
            this.privateKey = privateKey
            if (metadata.recipientTokenAddress.isNullOrEmpty()) {
                this.createAndTransferTokenTransaction = Solana.CreateAndTransferToken.newBuilder().apply {
                    this.amount = amount
                    this.decimals = decimals
                    this.recipientMainAddress = recipient
                    this.tokenMintAddress = tokenId
                    this.senderTokenAddress = metadata.senderTokenAddress
                    this.recipientTokenAddress = SolanaAddress(recipient).defaultTokenAddress(tokenId)
                    this.memo = input.input.memo() ?: ""
                }.build()
            } else {
                this.tokenTransferTransaction = Solana.TokenTransfer.newBuilder().apply {
                    this.amount = amount
                    this.decimals = decimals
                    this.tokenMintAddress = tokenId
                    this.senderTokenAddress = metadata.senderTokenAddress
                    this.recipientTokenAddress = metadata.recipientTokenAddress
                    this.memo = input.input.memo() ?: ""
                }.build()
            }
        }
    }

    private fun swap(input: SignerParams, privateKey: ByteArray): String {
        val swapParams = input.input as ConfirmParams.SwapParams
        val bytes = Base64.decode(swapParams.swapData)
        if (bytes[0] != 1.toByte()) {
            throw IllegalArgumentException("only support one signature")
        }
        val message = bytes.copyOfRange(65, bytes.size)
        val signature = PrivateKey(privateKey).sign(message, Curve.CURVE25519)
        val signed = byteArrayOf(0x1)
        return Base64.encode(signed + signature + message)
    }

    private fun sign(message: MessageLite): ByteArray {
        val output = AnySigner.sign(message, CoinType.SOLANA, Solana.SigningOutput.parser())
        val data = Base58.decodeNoCheck(output.encoded) ?: throw IllegalStateException("string is not Base58 encoding!")
        val base64 = Base64.encode(data)
        val offset = base64.length % 4
        return (if (offset == 0) base64 else base64.padStart(base64.length + 4 - offset, '='))
            .toByteArray()
    }

    override fun maintainChain(): Chain = Chain.Solana
}
