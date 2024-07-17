package com.gemwallet.android.blockchain.clients.sui

import com.gemwallet.android.blockchain.clients.SignerPreload
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.ext.type
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerInputInfo
import com.gemwallet.android.model.SignerParams
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import uniffi.Gemstone.SuiCoin
import uniffi.Gemstone.SuiGas
import uniffi.Gemstone.SuiObjectRef
import uniffi.Gemstone.SuiStakeInput
import uniffi.Gemstone.SuiTokenTransferInput
import uniffi.Gemstone.SuiTransferInput
import uniffi.Gemstone.SuiTxOutput
import uniffi.Gemstone.SuiUnstakeInput
import uniffi.Gemstone.suiEncodeSplitStake
import uniffi.Gemstone.suiEncodeTokenTransfer
import uniffi.Gemstone.suiEncodeTransfer
import uniffi.Gemstone.suiEncodeUnstake
import uniffi.Gemstone.suiValidateAndHash
import wallet.core.jni.Base64
import java.math.BigInteger

class SuiSignerPreloader(
    private val chain: Chain,
    private val rpcClient: SuiRpcClient,
) : SignerPreload {

    private val coinId = "0x2::sui::SUI"

    override suspend fun invoke(owner: Account, params: ConfirmParams): Result<SignerParams> {
        val txData = when (params) {
            is ConfirmParams.TransferParams -> when (params.assetId.type()) {
                AssetSubtype.NATIVE -> encodeTransfer(
                    sender = owner.address,
                    recipient = params.destination.address,
                    value = params.amount,
                    coinType = coinId,
                    sendMax = params.isMax(),
                )
                AssetSubtype.TOKEN -> encodeTokenTransfer(
                    sender = owner.address,
                    recipient = params.destination.address,
                    value = params.amount,
                    coinType = params.assetId.tokenId!!,
                    gasCoinType = coinId
                )
            }
            is ConfirmParams.DelegateParams -> encodeStake(
                sender = owner.address,
                validator = params.validatorId,
                coinType = coinId,
                value = params.amount,
            )
            is ConfirmParams.UndelegateParams -> encodeUnstake(
                sender = owner.address,
                coinType = coinId,
                stakeId = params.delegationId,
            )
            is ConfirmParams.SwapParams -> encodeSwap(params)
            is ConfirmParams.RewardsParams,
            is ConfirmParams.RedeleateParams,

            is ConfirmParams.TokenApprovalParams,
            is ConfirmParams.WithdrawParams -> throw java.lang.IllegalArgumentException()
        }

        val fee = SuiFee().invoke(rpcClient, owner, Base64.encode(txData.txData))
        return Result.success(
            SignerParams(
                input = params,
                owner = owner.address,
                info = Info(
                    messageBytes = "${Base64.encode(txData.txData)}_${txData.hash.toHexString()}",
                    fee = fee,
                )
            )
        )
    }

    override fun maintainChain(): Chain = chain

    private fun gasBudget(coinType: String): BigInteger = BigInteger.valueOf(25_000_000)

    private suspend fun encodeTransfer(
        sender: String,
        recipient: String,
        coinType: String,
        value: BigInteger,
        sendMax: Boolean
    ) = withContext(Dispatchers.IO) {
        val getCoins = async { rpcClient.coins(sender, coinType).getOrThrow().result.data }
        val getGasPrice = async { rpcClient.gasPrice(JSONRpcRequest.create(SuiMethod.GasPrice, emptyList())).getOrNull()?.result ?: "750" }
        val coins = getCoins.await()
        val gasPrice = getGasPrice.await()
        val input = SuiTransferInput(
            sender = sender,
            recipient = recipient,
            amount = value.toLong().toULong(),
            coins = coins.map { it.toGemstone() },
            sendMax = sendMax,
            gas = SuiGas(
                budget = gasBudget(coinType).toLong().toULong(),
                price = gasPrice.toULong(),
            )
        )
        suiEncodeTransfer(input)
    }


    private suspend fun encodeTokenTransfer(
        sender: String,
        recipient: String,
        coinType: String,
        gasCoinType: String,
        value: BigInteger,
    ) = withContext(Dispatchers.IO) {
        val getCoins = async {
            rpcClient.coins(sender, coinType).getOrThrow().result.data
        }
        val getGasCoins = async {
            rpcClient.coins(sender, gasCoinType).getOrThrow().result.data
        }
        val getGasPrice = async { rpcClient.gasPrice(JSONRpcRequest.create(SuiMethod.GasPrice, emptyList())).getOrNull()?.result ?: "750" }
        val coins = getCoins.await()
        val gasPrice = getGasPrice.await()
        val gasCoins = getGasCoins.await()
        val gas = gasCoins.firstOrNull() ?: throw IllegalStateException("no gas coin")
        val input = SuiTokenTransferInput(
            sender = sender,
            recipient = recipient,
            amount = value.toLong().toULong(),
            tokens = coins.map { it.toGemstone() },
            gas = SuiGas(
                budget = gasBudget(gasCoinType).toLong().toULong(),
                price = gasPrice.toULong(),
            ),
            gasCoin = gas.toGemstone(),
        )
        suiEncodeTokenTransfer(input)
    }

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
            coins = coins.map { it.toGemstone() },
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
            gasCoin = coins.map { it.toGemstone() }.first(),
            gas = SuiGas(
                budget = gasBudget(coinType).toLong().toULong(),
                price = gasPrice.toULong(),
            )
        )
        suiEncodeUnstake(input)
    }

    private fun encodeSwap(input: ConfirmParams.SwapParams): SuiTxOutput {
        return suiValidateAndHash(input.swapData)
    }

    data class Info(
        val messageBytes: String,
        val fee: Fee,
    ) : SignerInputInfo {
        override fun fee(): Fee = fee
    }
    
    fun com.wallet.core.blockchain.sui.SuiCoin.toGemstone() = SuiCoin(
        coinType = coinType,
        balance = balance.toULong(),
        objectRef = SuiObjectRef(
            objectId = coinObjectId,
            digest = digest,
            version = version.toULong(),
        )
    )
}