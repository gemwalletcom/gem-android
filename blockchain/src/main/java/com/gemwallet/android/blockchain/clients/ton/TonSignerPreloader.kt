package com.gemwallet.android.blockchain.clients.ton

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.SwapTransactionPreloader
import com.gemwallet.android.blockchain.clients.TokenTransferPreloader
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

internal const val tokenAccountCreationKey: String = "tokenAccountCreation"

class TonSignerPreloader(
    private val chain: Chain,
    private val rpcClient: TonRpcClient,
) : NativeTransferPreloader, TokenTransferPreloader, SwapTransactionPreloader {

    private val feeCalculator = TonFeeCalculator(chain, rpcClient)

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams {
        val fee = feeCalculator.calculateNative()
        val seqno = rpcClient.walletInfo(params.from.address).getOrNull()?.result?.seqno ?: 0
        return SignerParams(
            input = params,
            chainData = TonChainData(seqno, fee)
        )
    }

    override suspend fun preloadTokenTransfer(params: ConfirmParams.TransferParams.Token): SignerParams = withContext(Dispatchers.IO) {
        val getWalletInfo = async { rpcClient.walletInfo(params.from.address).getOrNull() }
        val getJettonAddress = async { jettonAddress(rpcClient, params.assetId.tokenId!!, params.from.address) }
        val getFee = async { feeCalculator.calculateToken(params.assetId, params.destination().address, params.memo()) }

        val seqno = getWalletInfo.await()?.result?.seqno ?: 0
        val jettonAddress = getJettonAddress.await() ?: throw Exception("can't get jetton address. check internet.")
        val fee = getFee.await()

        SignerParams(
            input = params,
            chainData = TonChainData(seqno, fee, jettonAddress)
        )
    }

    override suspend fun preloadSwap(params: ConfirmParams.SwapParams): SignerParams {
        val fee = feeCalculator.calculateSwap()
        val seqno = rpcClient.walletInfo(params.from.address).getOrNull()?.result?.seqno ?: 0
        return SignerParams(
            input = params,
            chainData = TonChainData(seqno, fee)
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    data class TonChainData(
        val sequence: Int,
        val fee: Fee,
        val jettonAddress: String? = null,
        val expireAt: Int? = null
    ) : ChainSignData {
        override fun fee(speed: TxSpeed): Fee = fee
    }
}