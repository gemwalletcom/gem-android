package com.gemwallet.android.blockchain.clients.stellar

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.stellar.services.StellarService
import com.gemwallet.android.blockchain.clients.stellar.services.accounts
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerParams
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.math.BigInteger

class StellarSignPreloadClient(
    private val chain: Chain,
    private val client: StellarService,
) : NativeTransferPreloader {

    private val feeCalculator = StellarFeeCalculator(chain, client)

    override suspend fun preloadNativeTransfer(
        params: ConfirmParams.TransferParams.Native
    ): SignerParams = withContext(Dispatchers.IO) {
        val getAccount = async { client.accounts(params.from.address) }
        val getIsDestinationAccountExist = async {
            try {
                client.accounts(params.destination.address) != null
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
                    priority = it.priority,
                    feeAssetId = it.feeAssetId,
                    amount = it.amount,
                    options = mapOf(StellarChainData.tokenAccountCreation to BigInteger.ZERO)
                )
            }
        }
        SignerParams(
            input = params,
            chainData = StellarChainData(
                sequence = account.sequence.toULong() + 1UL,
                isDestinationAddressExist = true,
            ),
            fee = fees,
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

}