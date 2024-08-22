package com.gemwallet.android.blockchain.clients.bitcoin

import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.TxSpeed
import com.gemwallet.android.model.TxSpeed.Fast
import com.gemwallet.android.model.TxSpeed.Normal
import com.gemwallet.android.model.TxSpeed.Slow
import com.wallet.core.blockchain.bitcoin.models.BitcoinUTXO
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import wallet.core.java.AnySigner
import wallet.core.jni.BitcoinSigHashType
import wallet.core.jni.CoinTypeConfiguration
import wallet.core.jni.proto.Bitcoin
import wallet.core.jni.proto.Common
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

class BitcoinFee {
    suspend operator fun invoke(
        rpcClient: BitcoinRpcClient,
        utxos: List<BitcoinUTXO>,
        account: Account,
        recipient: String,
        amount: BigInteger,
    ): List<Fee> {
        val ownerAddress = account.address
        val chain = account.chain
        val fee = TxSpeed.entries.map {
            val price = estimateFeePrice(chain, it, rpcClient)
            val limit = calcFee(chain, ownerAddress, recipient, amount.toLong(), price.toLong(), utxos)
            GasFee(
                feeAssetId = AssetId(chain),
                speed = it,
                maxGasPrice = price,
                limit = limit
            )
        }
        return fee
    }

    private suspend fun estimateFeePrice(chain: Chain, speed: TxSpeed, rpcClient: BitcoinRpcClient): BigInteger {
        val decimals = CoinTypeConfiguration.getDecimals(WCChainTypeProxy().invoke(chain))
        val minimumByteFee = getMinimumByteFee(chain)
        return rpcClient.estimateFee(getFeePriority(chain, speed)).fold(
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
        val total = utxos.map { it.value.toLong() }.reduce { x, y -> x + y }
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
        if (plan.error == Common.SigningError.Error_not_enough_utxos || plan.error == Common.SigningError.Error_missing_input_utxos) {
            throw IllegalStateException("Dust Error: $bytePrice")
        } else if (plan.error != Common.SigningError.OK) {
            throw IllegalStateException(plan.error.name)
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

    private fun getMinimumByteFee(chain: Chain) = when (chain) {
        Chain.Litecoin -> BigInteger("5")
        Chain.Doge -> BigInteger("1000")
        else -> BigInteger.ONE
    }

    private fun getFeePriority(chain: Chain, speed: TxSpeed) = when (chain) {
        Chain.Litecoin -> when (speed) {
            Fast -> 1
            Normal -> 3
            Slow -> 6
        }
        Chain.Doge -> when (speed) {
            Fast -> 2
            Normal -> 4
            Slow -> 8
        }
        else -> when (speed) {
            Fast -> 1
            Normal -> 3
            Slow -> 6
        }
    }.toString()
}