package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Chain
import java.math.BigInteger

interface SignClient : BlockchainClient {

    suspend fun signMessage(
        chain: Chain,
        input: ByteArray,
        privateKey: ByteArray,
    ): ByteArray = byteArrayOf()

    suspend fun signTypedMessage(chain: Chain, input: ByteArray, privateKey: ByteArray): ByteArray = byteArrayOf()

    suspend fun signTransaction(
        params: SignerParams,
        txSpeed: TxSpeed = TxSpeed.Normal,
        privateKey: ByteArray,
    ): List<ByteArray> = throw IllegalArgumentException("Deprecated")

    suspend fun sign(
        params: ConfirmParams.TransferParams.Native,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray,
    ): List<ByteArray> = emptyList()

    suspend fun sign(
        params: ConfirmParams.TransferParams.Token,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray,
    ): List<ByteArray> = emptyList()

    suspend fun sign(
        params: ConfirmParams.SwapParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray,
    ): List<ByteArray> = emptyList()

    suspend fun sign(
        params: ConfirmParams.TokenApprovalParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray,
    ): List<ByteArray> = emptyList()

    suspend fun sign(
        params: ConfirmParams.Stake.DelegateParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray,
    ): List<ByteArray> = emptyList()

    suspend fun sign(
        params: ConfirmParams.Stake.UndelegateParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray,
    ): List<ByteArray> = emptyList()

    suspend fun sign(
        params: ConfirmParams.Stake.RedelegateParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray,
    ): List<ByteArray> = emptyList()

    suspend fun sign(
        params: ConfirmParams.Stake.RewardsParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray,
    ): List<ByteArray> = emptyList()

    suspend fun sign(
        params: ConfirmParams.Stake.WithdrawParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray,
    ): List<ByteArray> = emptyList()
}