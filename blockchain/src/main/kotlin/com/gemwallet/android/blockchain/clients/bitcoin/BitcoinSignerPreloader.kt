package com.gemwallet.android.blockchain.clients.bitcoin

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.SwapTransactionPreloader
import com.gemwallet.android.blockchain.clients.bitcoin.services.BitcoinFeeService
import com.gemwallet.android.blockchain.clients.bitcoin.services.BitcoinUTXOService
import com.gemwallet.android.ext.fullAddress
import com.gemwallet.android.ext.toBitcoinChain
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerParams
import com.wallet.core.blockchain.bitcoin.models.BitcoinUTXO
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority
import java.math.BigInteger

class BitcoinSignerPreloader(
    private val chain: Chain,
    private val utxoService: BitcoinUTXOService,
    feeService: BitcoinFeeService,
) : NativeTransferPreloader, SwapTransactionPreloader {

    private val feeCalculator = BitcoinFeeCalculator(feeService)

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams {
        val data = preload(params.from, params.destination().address, params.amount)
        return SignerParams(params, data)
    }

    override suspend fun preloadSwap(params: ConfirmParams.SwapParams): SignerParams {
        val data = preload(params.from, params.destination().address, params.amount)
        return SignerParams(params, data)
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    private suspend fun preload(fromAccount: Account, destinationAddress: String, amount: BigInteger): BitcoinChainData {
        val address = chain.toBitcoinChain().fullAddress(fromAccount.address)
        val utxo = utxoService.getUTXO(address).getOrNull() ?: throw Exception("Can't load UTXO")
        val fee = feeCalculator.calculate(utxo, fromAccount, destinationAddress, amount)
        return BitcoinChainData(utxo, listOf(fee.firstOrNull{ it.priority == FeePriority.Fast } ?: fee.last())) // BTC Transaction is slow.
    }

    data class BitcoinChainData(
        val utxo: List<BitcoinUTXO>,
        val fee: List<Fee>,
    ) : ChainSignData {
        override fun fee(speed: FeePriority): Fee = fee.firstOrNull { it.priority == speed } ?: fee.first()

        override fun allFee(): List<Fee> = fee
    }
}