package com.gemwallet.android.blockchain.clients.bitcoin

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.bitcoin.services.BitcoinFeeService
import com.gemwallet.android.blockchain.clients.bitcoin.services.BitcoinUTXOService
import com.gemwallet.android.ext.fullAddress
import com.gemwallet.android.ext.toBitcoinChain
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.blockchain.bitcoin.models.BitcoinUTXO
import com.wallet.core.primitives.Chain
import java.lang.Exception

class BitcoinSignerPreloader(
    private val chain: Chain,
    private val utxoService: BitcoinUTXOService,
    feeService: BitcoinFeeService,
) : NativeTransferPreloader {

    private val feeCalculator = BitcoinFeeCalculator(feeService)

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams {
        val address = chain.toBitcoinChain().fullAddress(params.from.address)
        val utxo = utxoService.getUTXO(address).getOrNull() ?: throw Exception("Can't load UTXO")
        val fee = feeCalculator.calculate(utxo, params.from, params.destination().address, params.amount)
        return SignerParams(params, BitcoinChainData(utxo, fee))
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    data class BitcoinChainData(
        val utxo: List<BitcoinUTXO>,
        val fee: List<Fee>,
    ) : ChainSignData {
        override fun fee(speed: TxSpeed): Fee = fee.firstOrNull { it.speed == speed } ?: fee.first()

        override fun allFee(): List<Fee> = fee
    }
}