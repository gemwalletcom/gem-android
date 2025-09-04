package com.gemwallet.android.blockchain.clients.bitcoin

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.SwapTransactionPreloader
import com.gemwallet.android.blockchain.clients.bitcoin.services.BitcoinRpcClient
import com.gemwallet.android.ext.fullAddress
import com.gemwallet.android.ext.toBitcoinChain
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.SignerParams
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.UTXO
import java.math.BigInteger

class BitcoinSignerPreloader(
    private val chain: Chain,
    private val client: BitcoinRpcClient,
) : NativeTransferPreloader, SwapTransactionPreloader {

    private val feeCalculator = BitcoinFeeCalculator(client)

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams {
        val data = preload(params, params.from, params.destination().address, params.amount)
        return data
    }

    override suspend fun preloadSwap(params: ConfirmParams.SwapParams): SignerParams {
        val data = preload(params, params.from, params.destination().address, params.amount)
        return data
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    private suspend fun preload(params: ConfirmParams, fromAccount: Account, destinationAddress: String, amount: BigInteger): SignerParams {
        val address = chain.toBitcoinChain().fullAddress(fromAccount.address)
        val utxo = client.getUTXO(address).getOrNull()?.map {
            UTXO(
                it.txid,
                it.vout,
                it.value,
                ""
            )
        } ?: throw Exception("Can't load UTXO")
        val fee = feeCalculator.calculate(utxo, fromAccount, destinationAddress, amount)
        return SignerParams(
            params,
            BitcoinChainData(utxo),
            fee,
        )
    }

}