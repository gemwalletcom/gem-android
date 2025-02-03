package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.operators.GetAsset
import com.gemwallet.android.ext.type
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.google.protobuf.ByteString
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.SolanaTokenProgramId
import com.wallet.core.primitives.TransactionType
import wallet.core.java.AnySigner
import wallet.core.jni.Base58
import wallet.core.jni.Base64
import wallet.core.jni.CoinType
import wallet.core.jni.Curve
import wallet.core.jni.PrivateKey
import wallet.core.jni.SolanaAddress
import wallet.core.jni.SolanaTransaction
import wallet.core.jni.TransactionDecoder
import wallet.core.jni.proto.Solana
import java.math.BigInteger

class SolanaSignClient(
    private val chain: Chain,
    private val getAsset: GetAsset,
) : SignClient {

    override suspend fun signMessage(chain: Chain, input: ByteArray, privateKey: ByteArray): ByteArray {
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

    override suspend fun signTransaction(
        params: SignerParams,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): List<ByteArray> {
        val fee = params.chainData.fee(txSpeed) as GasFee
        val recentBlockhash = (params.chainData as SolanaSignerPreloader.SolanaChainData).blockhash

        return when(params.input.getTxType()) {
            TransactionType.Swap -> swap(params, txSpeed, privateKey)
            TransactionType.Transfer -> {
                val signInput = when (params.input.assetId.type()) {
                    AssetSubtype.NATIVE -> signNative(params)
                    AssetSubtype.TOKEN -> signToken(params)
                }
                signInput.privateKey = ByteString.copyFrom(privateKey)
                sign(input = signInput, fee = fee)
            }
            TransactionType.StakeDelegate -> {
                val signInput = Solana.SigningInput.newBuilder().apply {
                    this.recentBlockhash = recentBlockhash
                    this.delegateStakeTransaction = Solana.DelegateStake.newBuilder().apply {
                        this.validatorPubkey = (params.input as ConfirmParams.Stake.DelegateParams).validatorId
                        this.value = params.finalAmount.toLong()
                    }.build()
                    this.privateKey = ByteString.copyFrom(privateKey)
                }
                sign(input = signInput, fee = fee)
            }
            TransactionType.StakeUndelegate -> {
                val signInput = Solana.SigningInput.newBuilder().apply {
                    this.recentBlockhash = recentBlockhash
                    this.deactivateStakeTransaction = Solana.DeactivateStake.newBuilder().apply {
                        this.stakeAccount = (params.input as ConfirmParams.Stake.UndelegateParams).delegationId
                    }.build()
                    this.privateKey = ByteString.copyFrom(privateKey)
                }
                sign(input = signInput, fee = fee)
            }
            TransactionType.StakeWithdraw -> {
                val signInput = Solana.SigningInput.newBuilder().apply {
                    this.recentBlockhash = recentBlockhash
                    this.withdrawTransaction = Solana.WithdrawStake.newBuilder().apply {
                        stakeAccount = (params.input as ConfirmParams.Stake.WithdrawParams).delegationId
                        value = params.finalAmount.toLong()
                    }.build()
                    this.privateKey = ByteString.copyFrom(privateKey)
                }
                sign(input = signInput, fee = fee)
            }
            TransactionType.TokenApproval,
            TransactionType.StakeRedelegate,
            TransactionType.AssetActivation,
            TransactionType.TransferNFT,
            TransactionType.SmartContractCall,
            TransactionType.StakeRewards -> throw IllegalArgumentException()
        }
    }

    private fun signNative(input: SignerParams): Solana.SigningInput.Builder {
        val blockhash = (input.chainData as SolanaSignerPreloader.SolanaChainData).blockhash

        return Solana.SigningInput.newBuilder().apply {
            this.transferTransaction = Solana.Transfer.newBuilder().apply {
                this.recipient = input.input.destination()?.address
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
        val recipient = input.input.destination()?.address
        val metadata = input.chainData as SolanaSignerPreloader.SolanaChainData
        val tokenProgramId = when (metadata.tokenProgram) {
            SolanaTokenProgramId.Token -> Solana.TokenProgramId.TokenProgram
            SolanaTokenProgramId.Token2022 -> Solana.TokenProgramId.Token2022Program
        }
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
                    this.tokenProgramId = tokenProgramId
                }.build()
            } else {
                val walletAddress = SolanaAddress(recipient)

                val recipientTokenAddress = when (tokenProgramId) {
                    Solana.TokenProgramId.TokenProgram -> walletAddress.defaultTokenAddress(tokenId)
                    Solana.TokenProgramId.Token2022Program -> walletAddress.token2022Address(tokenId)
                    else -> throw IllegalArgumentException("Incorrect Solana program")
                }

                this.tokenTransferTransaction = Solana.TokenTransfer.newBuilder().apply {
                    this.amount = amount
                    this.decimals = decimals
                    this.tokenMintAddress = tokenId
                    this.senderTokenAddress = metadata.senderTokenAddress
                    this.recipientTokenAddress = recipientTokenAddress
                    this.memo = input.input.memo() ?: ""
                    this.tokenProgramId = tokenProgramId
                }.build()
            }
        }
    }

    private fun swap(input: SignerParams, txSpeed: TxSpeed, privateKey: ByteArray): List<ByteArray> {
        val fee = input.chainData.fee(txSpeed) as? GasFee ?: throw java.lang.IllegalArgumentException("Incorrect fee data")
        val feePrice = fee.minerFee
        val feeLimit = fee.limit
        val swapParams = input.input as ConfirmParams.SwapParams

        val rawTx = SolanaTransaction.setComputeUnitPrice(swapParams.swapData, feePrice.toString()).ifEmpty {
            throw IllegalStateException("Unable to set compute unit price")
        }
        val transaction = SolanaTransaction.setComputeUnitLimit(rawTx, feeLimit.toString()).ifEmpty {
            throw IllegalStateException("Unable to set compute unit limit")
        }
        val transactionData = Base64.decode(transaction)

        val decodeOutputData = TransactionDecoder.decode(CoinType.SOLANA, transactionData)
        val decodeOutput = Solana.DecodingTransactionOutput.parseFrom(decodeOutputData)
        val signingInput = Solana.SigningInput.newBuilder().apply {
            this.privateKey = ByteString.copyFrom(privateKey)
            this.rawMessage = decodeOutput.transaction
            this.txEncoding = Solana.Encoding.Base64
        }.build()
        val output: Solana.SigningOutput = AnySigner.sign(signingInput, CoinType.SOLANA, Solana.SigningOutput.parser())
        if (!output.errorMessage.isNullOrEmpty()) {
            throw Exception(output.errorMessage)
        }
        return listOf(output.encoded.toByteArray())
    }

    private fun sign(input: Solana.SigningInput.Builder, fee: GasFee): List<ByteArray> {
        input.apply {
            this.priorityFeeLimit = Solana.PriorityFeeLimit.newBuilder().apply {
                this.limit = fee.limit.toInt()
            }.build()
            if (fee.minerFee > BigInteger.ZERO) {
                this.priorityFeePrice = Solana.PriorityFeePrice.newBuilder().apply {
                    this.price = fee.minerFee.toLong()
                }.build()
            }
        }
        val output = AnySigner.sign(input.build(), CoinType.SOLANA, Solana.SigningOutput.parser())
        val data = Base58.decodeNoCheck(output.encoded) ?: throw IllegalStateException("string is not Base58 encoding!")
        val base64 = Base64.encode(data)
        val offset = base64.length % 4
        val encodedOutput = (if (offset == 0) base64 else base64.padStart(base64.length + 4 - offset, '='))
            .toByteArray()
        return listOf(encodedOutput)
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}
