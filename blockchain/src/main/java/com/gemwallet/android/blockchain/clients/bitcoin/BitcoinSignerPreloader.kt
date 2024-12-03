package com.gemwallet.android.blockchain.clients.bitcoin

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.SignerPreload
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.blockchain.bitcoin.models.BitcoinUTXO
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import java.lang.Exception

class BitcoinSignerPreloader(
    private val chain: Chain,
    private val rpcClient: BitcoinRpcClient,
) : SignerPreload, NativeTransferPreloader {

    private val feeCalculator = BitcoinFeeCalculator(rpcClient)

    override suspend fun invoke(
        owner: Account,
        params: ConfirmParams,
    ): Result<SignerParams> {
        return rpcClient.getUTXO(owner.extendedPublicKey!!).mapCatching {
            val fee = feeCalculator.calculate(it, owner, params.destination()?.address!!, params.amount)
            SignerParams(
                input = params,
                chainData = BtcChainData(it, fee)
            )
        }
    }

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams): SignerParams {
        val utxo = rpcClient.getUTXO(params.from.extendedPublicKey!!).getOrNull()
            ?: throw Exception("Can't load UTXO")
        val fee = feeCalculator.calculate(utxo, params.from, params.destination().address, params.amount)
        return SignerParams(params, BtcChainData(utxo, fee))
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    data class BtcChainData(
        val utxo: List<BitcoinUTXO>,
        val fee: List<Fee>,
    ) : ChainSignData {
        override fun fee(speed: TxSpeed): Fee = fee.firstOrNull { it.speed == speed } ?: fee.first()

        override fun allFee(): List<Fee> = fee
    }
}