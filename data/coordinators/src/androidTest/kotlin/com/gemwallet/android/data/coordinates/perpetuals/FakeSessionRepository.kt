package com.gemwallet.android.data.coordinates.perpetuals

import com.gemwallet.android.data.repositoreis.perpetual.FakePerpetualRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.model.Session
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.PerpetualDirection
import com.wallet.core.primitives.PerpetualMarginType
import com.wallet.core.primitives.PerpetualPosition
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletSource
import com.wallet.core.primitives.WalletType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeSessionRepository(
    private val testAccountAddress: String = "test-account-address"
) : SessionRepository {
    private val sessionFlow = MutableStateFlow<Session?>(
        Session(
            wallet = Wallet(
                id = "test-wallet-id",
                name = "Test Wallet",
                type = WalletType.multicoin,
                accounts = listOf(
                    Account(
                        chain = Chain.Bitcoin,
                        address = testAccountAddress,
                        derivationPath = "m/84'/0'/0'/0/0",
                        extendedPublicKey = "",
                    )
                ),
                index = 0,
                order = 0,
                isPinned = false,
                source = WalletSource.Create
            ),
            currency = Currency.USD
        )
    )

    private val currencyFlow = MutableStateFlow(Currency.USD)

    override fun session(): StateFlow<Session?> = sessionFlow

    override suspend fun setWallet(wallet: Wallet) {
        val current = sessionFlow.value
        sessionFlow.value = current?.copy(wallet = wallet)
    }

    override suspend fun setCurrency(currency: Currency) {
        val current = sessionFlow.value
        sessionFlow.value = current?.copy(currency = currency)
        currencyFlow.value = currency
    }

    override suspend fun reset() {
        sessionFlow.value = null
    }

    override suspend fun getCurrentCurrency(): Currency = currencyFlow.value

    override fun getCurrency(): Flow<Currency> = currencyFlow
}

suspend fun setupTestPositions(
    repository: FakePerpetualRepository,
    accountAddress: String = "test-account-address"
) {
    val positions = listOf(
        PerpetualPosition(
            id = "pos-btc-1",
            perpetualId = "BTC-PERP",
            assetId = AssetId(Chain.Bitcoin),
            size = 1.0,
            sizeValue = 94960.25,
            leverage = 10u,
            entryPrice = 94960.25,
            liquidationPrice = 85464.225,
            marginType = PerpetualMarginType.Cross,
            direction = PerpetualDirection.Long,
            marginAmount = 4771.0,
            takeProfit = null,
            stopLoss = null,
            pnl = 460.25,
            funding = null
        ),
        PerpetualPosition(
            id = "pos-eth-1",
            perpetualId = "ETH-PERP",
            assetId = AssetId(Chain.Ethereum),
            size = 10.0,
            sizeValue = 36380.0,
            leverage = 5u,
            entryPrice = 3638.0,
            liquidationPrice = 4001.8,
            marginType = PerpetualMarginType.Cross,
            direction = PerpetualDirection.Short,
            marginAmount = 3625.0,
            takeProfit = null,
            stopLoss = null,
            pnl = -121.25,
            funding = null
        ),
    )

    repository.putPositions(accountAddress, positions)
}
