package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.operators.GetAsset
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.serializer.jsonEncoder
import com.google.protobuf.ByteString
import com.wallet.core.blockchain.solana.SolanaAccountMeta
import com.wallet.core.blockchain.solana.SolanaInstruction
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.SolanaTokenProgramId
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

    override suspend fun signGenericTransfer(
        params: ConfirmParams.TransferParams.Generic,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val input = params.memo ?: throw java.lang.IllegalArgumentException("No data")
        val bytes = Base64.decode(input)
        val numRequiredSignatures = bytes.firstOrNull()?.toInt() ?: throw IllegalArgumentException("Bad sign data")
        val signatures = (0..<numRequiredSignatures).map { signatureIndex ->
            val startOffset = signatureIndex * 64 + 1
            val endOffset = startOffset + 64
            bytes.copyOfRange(startOffset, endOffset)
        }
        val message = bytes.copyOfRange(signatures.size * 64 + 1, bytes.size)
        val signature = PrivateKey(privateKey).sign(message, Curve.ED25519)

        val sign = when (params.inputType!!) {
            ConfirmParams.TransferParams.InputType.Signature -> {
                val hash = Base58.encodeNoCheck(signature)
                hash.toByteArray()
            }
            ConfirmParams.TransferParams.InputType.EncodeTransaction -> Base64.encode(
                (signatures.toMutableList().apply { set(0, signature) } + listOf(message))
                    .fold(byteArrayOf()) { initial, value -> initial + value }
            ).toByteArray()
        }
        return listOf(sign)
    }

    override suspend fun signNativeTransfer(
        params: ConfirmParams.TransferParams.Native,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val blockhash = (chainData as SolanaChainData).blockhash
        val input = Solana.SigningInput.newBuilder().apply {
            this.transferTransaction = Solana.Transfer.newBuilder().apply {
                this.recipient = params.destination().address
                this.value = finalAmount.toLong()
                if (!params.memo().isNullOrEmpty()) {
                    this.memo = params.memo()
                }
            }.build()
            this.recentBlockhash = blockhash
            this.privateKey = ByteString.copyFrom(privateKey)
        }
        return sign(input = input, fee = fee as GasFee)
    }

    override suspend fun signTokenTransfer(
        params: ConfirmParams.TransferParams.Token,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val decimals = getAsset.getAsset(params.assetId)?.decimals ?: throw IllegalArgumentException("Asset not found")
        val tokenId = params.assetId.tokenId
        val amount = finalAmount.toLong()
        val recipient = params.destination().address
        val metadata = chainData as SolanaChainData
        val tokenProgramId = when (metadata.tokenProgram) {
            SolanaTokenProgramId.Token -> Solana.TokenProgramId.TokenProgram
            SolanaTokenProgramId.Token2022 -> Solana.TokenProgramId.Token2022Program
            null -> throw IllegalArgumentException("Not token program id")
        }
        val input = Solana.SigningInput.newBuilder().apply {
            this.recentBlockhash = metadata.blockhash
            this.privateKey = ByteString.copyFrom(privateKey)
            if (metadata.recipientTokenAddress.isNullOrEmpty()) {
                this.createAndTransferTokenTransaction = Solana.CreateAndTransferToken.newBuilder().apply {
                    this.amount = amount
                    this.decimals = decimals
                    this.recipientMainAddress = recipient
                    this.tokenMintAddress = tokenId
                    this.senderTokenAddress = metadata.senderTokenAddress
                    this.recipientTokenAddress = SolanaAddress(recipient).defaultTokenAddress(tokenId)
                    this.memo = params.memo() ?: ""
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
                    this.memo = params.memo() ?: ""
                    this.tokenProgramId = tokenProgramId
                }.build()
            }
        }
        return sign(input, fee as GasFee)
    }

    override suspend fun signSwap(
        params: ConfirmParams.SwapParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val fee = fee as GasFee
        val feePrice = fee.minerFee
        val feeLimit = fee.limit
        val encodedTx = params.swapData
        val encodedTxData = Base64.decode(encodedTx)

        encodedTxData?.takeIf { it.isNotEmpty() } ?: throw Exception("unable to decode base64 string or empty transaction data")
        val rawTxDecoder = SolanaRawTxDecoder(encodedTxData)
        val numRequiredSignature = rawTxDecoder.signatureCount()
        // other signers' signatures already prefilled, changing instructions would lead signature verification failure
        if (numRequiredSignature > 1) {
            return signRawTransaction(encodedTx, privateKey)
        }
        // Only user's signature is needed, safe to modifiy instructions
        val rawTx = SolanaTransaction.setComputeUnitPrice(params.swapData, feePrice.toString()).ifEmpty {
            throw IllegalStateException("Unable to set compute unit price")
        }
        val transaction = SolanaTransaction.setComputeUnitLimit(rawTx, feeLimit.toString()).ifEmpty {
            throw IllegalStateException("Unable to set compute unit limit")
        }
        return signRawTransaction(transaction, privateKey)
    }

    override suspend fun signDelegate(
        params: ConfirmParams.Stake.DelegateParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val instruction =  SolanaInstruction(
            programId = "MemoSq4gqABAXKb96qnH8TysNcWxMyWCqXgDLGmfcHr",
            accounts = listOf(
                SolanaAccountMeta(pubkey = params.from.address, isSigner = true, isWritable = true),
            ),
            data = Base58.encodeNoCheck((params.memo() ?: "").toByteArray())
        )
        val data = jsonEncoder.encodeToString(instruction)

        val recentBlockhash = (chainData as SolanaChainData).blockhash
        val signInput = Solana.SigningInput.newBuilder().apply {
            this.recentBlockhash = recentBlockhash
            this.delegateStakeTransaction = Solana.DelegateStake.newBuilder().apply {
                this.validatorPubkey = params.validator.id
                this.value = finalAmount.toLong()
            }.build()
            this.privateKey = ByteString.copyFrom(privateKey)
        }
        val encoded = sign(input = signInput, fee = fee as GasFee)
        val transaction = SolanaTransaction.insertInstruction(String(encoded.firstOrNull() ?: throw Exception()), -1, data)
        return signRawTransaction(transaction, privateKey)
    }

    override suspend fun signUndelegate(
        params: ConfirmParams.Stake.UndelegateParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val recentBlockhash = (chainData as SolanaChainData).blockhash
        val signInput = Solana.SigningInput.newBuilder().apply {
            this.recentBlockhash = recentBlockhash
            this.deactivateStakeTransaction = Solana.DeactivateStake.newBuilder().apply {
                this.stakeAccount = params.delegation.base.delegationId
            }.build()
            this.privateKey = ByteString.copyFrom(privateKey)
        }
        return sign(input = signInput, fee = fee as GasFee)
    }

    override suspend fun signWithdraw(
        params: ConfirmParams.Stake.WithdrawParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val recentBlockhash = (chainData as SolanaChainData).blockhash
        val signInput = Solana.SigningInput.newBuilder().apply {
            this.recentBlockhash = recentBlockhash
            this.withdrawTransaction = Solana.WithdrawStake.newBuilder().apply {
                stakeAccount = params.delegation.base.delegationId
                value = finalAmount.toLong()
            }.build()
            this.privateKey = ByteString.copyFrom(privateKey)
        }
        return sign(input = signInput, fee = fee as GasFee)
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

    private fun signRawTransaction(transaction: String, privateKey: ByteArray): List<ByteArray> {
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

    override fun supported(chain: Chain): Boolean = this.chain == chain
}

//public struct SolanaRawTxDecoder {
//    let rawData: Data
//
//    /// Decode a “short-vec” (compact-U16) length at `offset`, advancing the offset.
//    private func decodeShortVecLength(offset: inout Int) -> UInt8 {
//        let byte = rawData[offset]
//        offset += 1
//        return byte & 0x7F
//    }
//
//    func signatureCount() -> UInt8 {
//        var offset = 0
//        return decodeShortVecLength(offset: &offset)
//    }
//
//    func signatures() -> [Data] {
//        var offset = 0
//        let count = decodeShortVecLength(offset: &offset)
//        var result: [Data] = []
//        for _ in 0..<count {
//            let sig = rawData.subdata(in: offset..<(offset + 64))
//            result.append(sig)
//            offset += 64
//        }
//        return result
//    }
//
//    /// The serialized message bytes that every signer signs.
//    /// (Everything after the sig-array: length byte + N×64-byte sigs.)
//    func messageData() -> Data {
//        var offset = 0
//        let sigCount = Int(decodeShortVecLength(offset: &offset))
//        offset += sigCount * 64
//        return rawData.subdata(in: offset..<rawData.count)
//    }
//}