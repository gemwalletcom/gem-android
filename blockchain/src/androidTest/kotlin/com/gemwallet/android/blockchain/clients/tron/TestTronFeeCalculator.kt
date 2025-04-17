package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.ext.asset
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.DestinationAddress
import com.wallet.core.blockchain.tron.models.TronAccount
import com.wallet.core.blockchain.tron.models.TronAccountUsage
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.math.BigInteger

class TestTronFeeCalculator {
    companion object {
        init {
            includeLibs()
        }
    }

    @Test
    fun testTron_native_transfer_fee() {
        val feeCalculator = TronFeeCalculator(
            chain = Chain.Tron,
            nodeStatusService = FakeTronNodeStatusService(),
            callService = FakeTronCallService()
        )

        val params: ConfirmParams.TransferParams.Native = ConfirmParams.Builder(
            Chain.Tron.asset(),
            Account(Chain.Tron, "TNLmo9j9AuGnnxibQUT13xoMGuUmNwxtkU", ""),
            BigInteger.valueOf(1_000_000)
        ).transfer(destination = DestinationAddress("THdHCD2miVcUdqAt32168a6wiyG5CE6WRY")) as ConfirmParams.TransferParams.Native
        val result = runBlocking {
            feeCalculator.calculate(
                params,
                TronAccount(address = "TNLmo9j9AuGnnxibQUT13xoMGuUmNwxtkU"),
                TronAccountUsage()
            )
        }
    }
}