package com.gemwallet.android.blockchain.clients.bitcoin

import com.gemwallet.android.blockchain.clients.bitcoin.services.BitcoinFeeService
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.ext.toBitcoinChain
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.GasFee
import com.wallet.core.blockchain.bitcoin.BitcoinUTXO
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.BitcoinChain
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import uniffi.gemstone.Config
import wallet.core.java.AnySigner
import wallet.core.jni.BitcoinSigHashType
import wallet.core.jni.CoinTypeConfiguration
import wallet.core.jni.proto.Bitcoin
import wallet.core.jni.proto.Common
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

class BitcoinFeeCalculator(
    private val feeService: BitcoinFeeService,
) {
    suspend fun calculate(
        utxos: List<BitcoinUTXO>,
        account: Account,
        recipient: String,
        amount: BigInteger,
    ): List<Fee> = withContext(Dispatchers.IO) {
        val ownerAddress = account.address
        val chain = account.chain
        FeePriority.entries.map {
            async {
                val price = estimateFeePrice(chain, it)
                val limit =
                    calcFee(chain, ownerAddress, recipient, amount.toLong(), price.toLong(), utxos)
                GasFee(
                    feeAssetId = AssetId(chain),
                    priority = it,
                    maxGasPrice = price,
                    limit = limit
                )
            }
        }.awaitAll()
    }

    private suspend fun estimateFeePrice(chain: Chain, speed: FeePriority): BigInteger {
        val decimals = CoinTypeConfiguration.getDecimals(WCChainTypeProxy().invoke(chain))
        val minimumByteFee = getMinimumByteFee(chain)
        val priority = Config().getBitcoinChainConfig(chain.toBitcoinChain().string).blocksFeePriority.let {
            when (speed) {
                FeePriority.Slow -> it.slow
                FeePriority.Normal -> it.normal
                FeePriority.Fast -> it.fast
            }
        }.toString()
        return feeService.estimateFee(priority).fold(
            {
                val networkFeePerKb = Crypto(it.result, decimals).atomicValue
                val feePerByte = networkFeePerKb.toBigDecimal().divide(BigDecimal(1000), RoundingMode.CEILING).toBigInteger()
                return minimumByteFee.max(feePerByte)
            }
        ) {
            minimumByteFee
        }
    }

    private fun calcFee(
        chain: Chain,
        from: String,
        to: String,
        amount: Long,
        bytePrice: Long,
        utxos: List<BitcoinUTXO>,
    ): BigInteger {
        val coinType = WCChainTypeProxy().invoke(chain)
        val total = utxos.map { it.value.toLong() }.fold(0L) { x, y -> x + y }
        if (total == 0L) {
            return BigInteger.ZERO // empty balance
        }
        val input = Bitcoin.SigningInput.newBuilder().apply {
            this.hashType = BitcoinSigHashType.ALL.value()
            this.byteFee = bytePrice
            this.amount = amount
            this.useMaxAmount = total == amount
            this.coinType = coinType.value()
            this.toAddress = to
            this.changeAddress = from
            this.addAllUtxo(utxos.getUtxoTransactions(from, coinType))
        }.build()

        val plan = AnySigner.plan(input, coinType, Bitcoin.TransactionPlan.parser())
        when (plan.error) {
            Common.SigningError.OK -> { /* continue */ }
            Common.SigningError.Error_not_enough_utxos,
            Common.SigningError.Error_dust_amount_requested,
            Common.SigningError.Error_missing_input_utxos -> throw IllegalStateException("Dust Error: $bytePrice")
            else -> throw IllegalStateException(plan.error.name)
        }

        val selectedUtxos: MutableList<BitcoinUTXO> = mutableListOf()
        for (raw in plan.utxosList) {
            input.utxoList?.indexOfFirst { it == raw }?.let {
                selectedUtxos.add(utxos[it])
            }
        }
        if (utxos.isNotEmpty() && selectedUtxos.isEmpty() && amount != total && amount <= total) {
            return calcFee(chain, from, to, total, bytePrice, utxos)
        }
        return BigInteger.valueOf(plan.fee / bytePrice)
    }

    companion object {
        fun getMinimumByteFee(chain: Chain) = when (chain.toBitcoinChain()) {
            BitcoinChain.Litecoin -> BigInteger("5")
            BitcoinChain.Doge -> BigInteger("1000")
            BitcoinChain.BitcoinCash,
            BitcoinChain.Bitcoin -> BigInteger.ONE
        }
    }
}