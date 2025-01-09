package com.gemwallet.android.blockchain.clients.stellar

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.stellar.services.StellarAccountService
import com.gemwallet.android.blockchain.clients.stellar.services.StellarFeeService
import com.gemwallet.android.blockchain.clients.stellar.services.accounts
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.math.BigInteger

class StellarSignPreloadClient(
    private val chain: Chain,
    private val accountService: StellarAccountService,
    feeService: StellarFeeService,
) : NativeTransferPreloader {

    private val feeCalculator = StellarFeeCalculator(chain, feeService)

    override suspend fun preloadNativeTransfer(
        params: ConfirmParams.TransferParams.Native
    ): SignerParams = withContext(Dispatchers.IO) {
        val getAccount = async { accountService.accounts(params.from.address) }
        val getIsDestinationAccountExist = async {
            try {
                accountService.accounts(params.destination.address) != null
            } catch (_: Throwable) {
                false
            }
        }
        val getDefaultFees = async { feeCalculator.calculate() }

        val account = getAccount.await()
        val isDestinationAccountExist = getIsDestinationAccountExist.await()
        val defaultFees = getDefaultFees.await()

        if (account?.sequence == null) {
            throw Exception("invalid sequence")
        }
        val fees = if (isDestinationAccountExist) {
            defaultFees
        } else {
            defaultFees.map {
                Fee(
                    speed = it.speed,
                    feeAssetId = it.feeAssetId,
                    amount = it.amount,
                    options = mapOf(StellarChainData.tokenAccountCreation to BigInteger.ZERO)
                )
            }
        }
        SignerParams(
            input = params,
            chainData = StellarChainData(
                fees = fees,
                sequence = account.sequence.toLong() + 1
            )
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    data class StellarChainData(
        val fees: List<Fee>,
        val sequence: Long,
    ) : ChainSignData {
        override fun fee(speed: TxSpeed): Fee = fees.firstOrNull { it.speed == speed } ?: fees.first()

        override fun allFee(): List<Fee> = fees

        companion object {
            const val tokenAccountCreation = "tokenAccountCreation"
        }
    }
}