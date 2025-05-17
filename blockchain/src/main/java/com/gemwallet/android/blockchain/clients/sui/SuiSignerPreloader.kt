package com.gemwallet.android.blockchain.clients.sui

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.StakeTransactionPreloader
import com.gemwallet.android.blockchain.clients.SwapTransactionPreloader
import com.gemwallet.android.blockchain.clients.TokenTransferPreloader
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerParams
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import uniffi.gemstone.SuiCoin
import uniffi.gemstone.SuiGas
import uniffi.gemstone.SuiObjectRef
import uniffi.gemstone.SuiStakeInput
import uniffi.gemstone.SuiTokenTransferInput
import uniffi.gemstone.SuiTransferInput
import uniffi.gemstone.SuiTxOutput
import uniffi.gemstone.SuiUnstakeInput
import uniffi.gemstone.suiEncodeSplitStake
import uniffi.gemstone.suiEncodeTokenTransfer
import uniffi.gemstone.suiEncodeTransfer
import uniffi.gemstone.suiEncodeUnstake
import uniffi.gemstone.suiValidateAndHash
import wallet.core.jni.Base64
import java.math.BigInteger

class SuiSignerPreloader(
    private val chain: Chain,
    private val rpcClient: SuiRpcClient,
) : NativeTransferPreloader, TokenTransferPreloader, StakeTransactionPreloader, SwapTransactionPreloader {

    private val feeCalculator = SuiFeeCalculator(rpcClient)

    private val coinId = "0x2::sui::SUI"

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams = withContext(Dispatchers.IO) {
        val sender = params.from.address
        val getCoins = async { rpcClient.coins(sender, coinId).getOrThrow().result.data }
        val getGasPrice = async { rpcClient.gasPrice(JSONRpcRequest.create(SuiMethod.GasPrice, emptyList())).getOrNull()?.result ?: "750" }
        val coins = getCoins.await()
        val gasPrice = getGasPrice.await()
        val input = SuiTransferInput(
            sender = sender,
            recipient = params.destination.address,
            amount = params.amount.toLong().toULong(),
            coins = coins.map { it.togemstone() },
            sendMax = params.isMax(),
            gas = SuiGas(
                budget = gasBudget(coinId).toLong().toULong(),
                price = gasPrice.toULong(),
            )
        )
        val data = suiEncodeTransfer(input)
        build(params, data)
    }

    override suspend fun preloadTokenTransfer(params: ConfirmParams.TransferParams.Token): SignerParams = withContext(Dispatchers.IO) {
        val sender = params.from.address
        val getCoins = async {
            rpcClient.coins(sender, params.assetId.tokenId!!).getOrThrow().result.data
        }
        val getGasCoins = async {
            rpcClient.coins(sender, coinId).getOrThrow().result.data
        }
        val getGasPrice = async { rpcClient.gasPrice(JSONRpcRequest.create(SuiMethod.GasPrice, emptyList())).getOrNull()?.result ?: "750" }
        val coins = getCoins.await()
        val gasPrice = getGasPrice.await()
        val gasCoins = getGasCoins.await()
        val gas = gasCoins.firstOrNull() ?: throw IllegalStateException("no gas coin")
        val input = SuiTokenTransferInput(
            sender = sender,
            recipient = params.destination.address,
            amount = params.amount.toLong().toULong(),
            tokens = coins.map { it.togemstone() },
            gas = SuiGas(
                budget = gasBudget(coinId).toLong().toULong(),
                price = gasPrice.toULong(),
            ),
            gasCoin = gas.togemstone(),
        )
        val data = suiEncodeTokenTransfer(input)
        build(params, data)
    }

    override suspend fun preloadStake(params: ConfirmParams.Stake): SignerParams {
        val data = when (params) {
            is ConfirmParams.Stake.DelegateParams -> encodeStake(
                sender = params.from.address,
                validator = params.validatorId,
                coinType = coinId,
                value = params.amount,
            )
            is ConfirmParams.Stake.UndelegateParams -> encodeUnstake(
                sender = params.from.address,
                coinType = coinId,
                stakeId = params.delegationId,
            )
            is ConfirmParams.Stake.RewardsParams,
            is ConfirmParams.Stake.RedelegateParams,
            is ConfirmParams.Stake.WithdrawParams -> throw IllegalArgumentException("Not supported")
        }
        return build(params, data)
    }

    override suspend fun preloadSwap(params: ConfirmParams.SwapParams): SignerParams {
        val data = suiValidateAndHash(params.swapData)
        return build(params, data)
    }

    private suspend fun build(params: ConfirmParams, data: SuiTxOutput): SignerParams {
        val fee = feeCalculator.calculate(params.from, Base64.encode(data.txData))

        return SignerParams(
            input = params,
            chainData = SuiChainData(
                messageBytes = "${Base64.encode(data.txData)}_${data.hash.toHexString()}",
                fee = fee,
            )
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    private fun gasBudget(coinType: String): BigInteger = BigInteger.valueOf(25_000_000)

    private suspend fun encodeStake(
        sender: String,
        coinType: String,
        value: BigInteger,
        validator: String,
    ) = withContext(Dispatchers.IO) {
        val getCoins = async { rpcClient.coins(sender, coinType).getOrThrow().result.data }
        val getGasPrice = async { rpcClient.gasPrice(JSONRpcRequest.create(SuiMethod.GasPrice, emptyList())).getOrNull()?.result ?: "750" }
        val coins = getCoins.await()
        val gasPrice = getGasPrice.await()
        val input = SuiStakeInput(
            sender = sender,
            validator = validator,
            stakeAmount = value.toLong().toULong(),
            coins = coins.map { it.togemstone() },
            gas = SuiGas(
                budget = gasBudget(coinType).toLong().toULong(),
                price = gasPrice.toULong(),
            )
        )
        suiEncodeSplitStake(input)
    }

    private suspend fun encodeUnstake(
        sender: String,
        coinType: String,
        stakeId: String,
    ) = withContext(Dispatchers.IO) {
        val getCoins = async { rpcClient.coins(sender, coinType).getOrThrow().result.data }
        val getGasPrice = async { rpcClient.gasPrice(JSONRpcRequest.create(SuiMethod.GasPrice, emptyList())).getOrNull()?.result ?: "750" }
        val getObject = async { rpcClient.getObject(JSONRpcRequest.create(SuiMethod.GetObject, listOf(stakeId))).getOrNull()?.result ?: throw IllegalStateException() }
        val coins = getCoins.await()
        val gasPrice = getGasPrice.await()
        val suiObject = getObject.await()

        val input = SuiUnstakeInput(
            sender = sender,
            stakedSui = SuiObjectRef(
                objectId = suiObject.data.objectId,
                digest = suiObject.data.digest,
                version = suiObject.data.version.toULong(),
            ),
            gasCoin = coins.map { it.togemstone() }.first(),
            gas = SuiGas(
                budget = gasBudget(coinType).toLong().toULong(),
                price = gasPrice.toULong(),
            )
        )
        suiEncodeUnstake(input)
    }

    data class SuiChainData(
        val messageBytes: String,
        val fee: Fee,
    ) : ChainSignData {
        override fun fee(speed: FeePriority): Fee = fee
    }

    private fun com.wallet.core.blockchain.sui.models.SuiCoin.togemstone() = SuiCoin(
        coinType = coinType,
        balance = balance.toULong(),
        objectRef = SuiObjectRef(
            objectId = coinObjectId,
            digest = digest,
            version = version.toULong(),
        )
    )
}