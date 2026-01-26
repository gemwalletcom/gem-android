package com.gemwallet.android.data.coordinates.pricealerts

import com.gemwallet.android.domains.price.PriceState
import com.gemwallet.android.domains.pricealerts.aggregates.PriceAlertType
import com.gemwallet.android.model.AssetPriceInfo
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetPrice
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.PriceAlert
import com.wallet.core.primitives.PriceAlertDirection
import org.junit.Assert.assertEquals
import org.junit.Test

class PriceAlertDataAggregateImplTest {

    private val btcAsset = Asset(
        id = AssetId(Chain.Bitcoin),
        name = "Bitcoin",
        symbol = "BTC",
        decimals = 8,
        type = AssetType.NATIVE,
    )

    private val ethAsset = Asset(
        id = AssetId(Chain.Ethereum),
        name = "Ethereum",
        symbol = "ETH",
        decimals = 18,
        type = AssetType.NATIVE,
    )

    private val solAsset = Asset(
        id = AssetId(Chain.Solana),
        name = "Solana",
        symbol = "sol",
        decimals = 9,
        type = AssetType.NATIVE,
    )

    private fun createAssetPrice(
        assetId: AssetId = btcAsset.id,
        price: Double = 45000.0,
        priceChangePercentage24h: Double = 5.2,
    ) = AssetPrice(
        assetId = assetId,
        price = price,
        priceChangePercentage24h = priceChangePercentage24h,
        updatedAt = System.currentTimeMillis(),
    )

    private fun createAssetPriceInfo(
        assetId: AssetId = btcAsset.id,
        price: Double = 45000.0,
        priceChangePercentage24h: Double = 5.2,
        currency: Currency = Currency.USD,
    ) = AssetPriceInfo(
        currency = currency,
        price = createAssetPrice(assetId, price, priceChangePercentage24h),
    )

    private fun createPriceAlert(
        assetId: AssetId = btcAsset.id,
        currency: String = "USD",
        price: Double? = null,
        pricePercentChange: Double? = null,
        priceDirection: PriceAlertDirection? = null,
    ) = PriceAlert(
        assetId = assetId,
        currency = currency,
        price = price,
        pricePercentChange = pricePercentChange,
        priceDirection = priceDirection,
        lastNotifiedAt = null,
    )

    private fun createAggregate(
        id: Int = 1,
        asset: Asset = btcAsset,
        assetPrice: AssetPriceInfo = createAssetPriceInfo(asset.id),
        priceAlert: PriceAlert = createPriceAlert(asset.id),
    ) = PriceAlertDataAggregateImpl(
        id = id,
        asset = asset,
        assetPrice = assetPrice,
        priceAlert = priceAlert,
    )

    @Test
    fun testBasicPropertyDelegation() {
        val aggregate = createAggregate(
            id = 123,
            asset = ethAsset,
        )

        assertEquals(123, aggregate.id)
        assertEquals(ethAsset.id, aggregate.assetId)
        assertEquals(ethAsset, aggregate.icon)
        assertEquals("Ethereum", aggregate.title)
    }

    @Test
    fun testTitleBadge_uppercase() {
        val aggregate = createAggregate(asset = solAsset)

        assertEquals("SOL", aggregate.titleBadge)
    }

    @Test
    fun testPriceState_directionUp() {
        val priceAlert = createPriceAlert(
            priceDirection = PriceAlertDirection.Up
        )
        val aggregate = createAggregate(priceAlert = priceAlert)

        assertEquals(PriceState.Up, aggregate.priceState)
    }

    @Test
    fun testPriceState_directionDown() {
        val priceAlert = createPriceAlert(
            priceDirection = PriceAlertDirection.Down
        )
        val aggregate = createAggregate(priceAlert = priceAlert)

        assertEquals(PriceState.Down, aggregate.priceState)
    }

    @Test
    fun testPriceState_alertPriceAboveCurrentPrice() {
        val assetPrice = createAssetPriceInfo(price = 45000.0)
        val priceAlert = createPriceAlert(
            price = 50000.0,
            priceDirection = null,
        )
        val aggregate = createAggregate(
            assetPrice = assetPrice,
            priceAlert = priceAlert,
        )

        assertEquals(PriceState.Up, aggregate.priceState)
    }

    @Test
    fun testPriceState_alertPriceBelowCurrentPrice() {
        val assetPrice = createAssetPriceInfo(price = 45000.0)
        val priceAlert = createPriceAlert(
            price = 40000.0,
            priceDirection = null,
        )
        val aggregate = createAggregate(
            assetPrice = assetPrice,
            priceAlert = priceAlert,
        )

        assertEquals(PriceState.Down, aggregate.priceState)
    }

    @Test
    fun testPriceState_noPriceNoDirection_positiveChange() {
        val assetPrice = createAssetPriceInfo(
            price = 45000.0,
            priceChangePercentage24h = 3.5,
        )
        val priceAlert = createPriceAlert(
            price = null,
            priceDirection = null,
        )
        val aggregate = createAggregate(
            assetPrice = assetPrice,
            priceAlert = priceAlert,
        )

        assertEquals(PriceState.Up, aggregate.priceState)
    }

    @Test
    fun testPriceState_noPriceNoDirection_negativeChange() {
        val assetPrice = createAssetPriceInfo(
            price = 45000.0,
            priceChangePercentage24h = -2.1,
        )
        val priceAlert = createPriceAlert(
            price = null,
            priceDirection = null,
        )
        val aggregate = createAggregate(
            assetPrice = assetPrice,
            priceAlert = priceAlert,
        )

        assertEquals(PriceState.Up, aggregate.priceState)
    }

    @Test
    fun testPrice_fromPriceAlert() {
        val priceAlert = createPriceAlert(
            price = 50000.0,
            currency = "USD",
        )
        val aggregate = createAggregate(priceAlert = priceAlert)

        assertEquals("$50,000.00", aggregate.price)
    }

    @Test
    fun testPrice_fromAssetPrice_whenAlertPriceNull() {
        val assetPrice = createAssetPriceInfo(
            price = 45234.50,
            currency = Currency.USD,
        )
        val priceAlert = createPriceAlert(price = null)
        val aggregate = createAggregate(
            assetPrice = assetPrice,
            priceAlert = priceAlert,
        )

        assertEquals("$45,234.50", aggregate.price)
    }

    @Test
    fun testPrice_withEuroCurrency() {
        val assetPrice = createAssetPriceInfo(
            price = 42000.0,
            currency = Currency.EUR,
        )
        val priceAlert = createPriceAlert(
            price = null,
            currency = "EUR",
        )
        val aggregate = createAggregate(
            assetPrice = assetPrice,
            priceAlert = priceAlert,
        )

        assertEquals("€42,000.00", aggregate.price)
    }

    @Test
    fun testPercentage_fromPriceAlert() {
        val priceAlert = createPriceAlert(
            pricePercentChange = 3.5,
        )
        val aggregate = createAggregate(priceAlert = priceAlert)

        assertEquals("3.50%", aggregate.percentage)
    }

    @Test
    fun testPercentage_fromAssetPrice_whenAlertPercentNull() {
        val assetPrice = createAssetPriceInfo(
            priceChangePercentage24h = -2.15,
        )
        val priceAlert = createPriceAlert(
            pricePercentChange = null,
        )
        val aggregate = createAggregate(
            assetPrice = assetPrice,
            priceAlert = priceAlert,
        )

        assertEquals("-2.15%", aggregate.percentage)
    }

    @Test
    fun testPercentage_largeValue() {
        val priceAlert = createPriceAlert(
            pricePercentChange = 125.67,
        )
        val aggregate = createAggregate(priceAlert = priceAlert)

        assertEquals("125.67%", aggregate.percentage)
    }

    @Test
    fun testType_directionUp() {
        val priceAlert = createPriceAlert(
            priceDirection = PriceAlertDirection.Up
        )
        val aggregate = createAggregate(priceAlert = priceAlert)

        assertEquals(PriceAlertType.Auto, aggregate.type)
    }

    @Test
    fun testType_directionDown() {
        val priceAlert = createPriceAlert(
            priceDirection = PriceAlertDirection.Down
        )
        val aggregate = createAggregate(priceAlert = priceAlert)

        assertEquals(PriceAlertType.Auto, aggregate.type)
    }

    @Test
    fun testType_alertPriceAboveCurrentPrice() {
        val assetPrice = createAssetPriceInfo(price = 45000.0)
        val priceAlert = createPriceAlert(
            price = 50000.0,
            priceDirection = null,
        )
        val aggregate = createAggregate(
            assetPrice = assetPrice,
            priceAlert = priceAlert,
        )

        assertEquals(PriceAlertType.Auto, aggregate.type)
    }

    @Test
    fun testType_alertPriceBelowCurrentPrice() {
        val assetPrice = createAssetPriceInfo(price = 45000.0)
        val priceAlert = createPriceAlert(
            price = 40000.0,
            priceDirection = PriceAlertDirection.Down,
        )
        val aggregate = createAggregate(
            assetPrice = assetPrice,
            priceAlert = priceAlert,
        )

        assertEquals(PriceAlertType.Under, aggregate.type)
    }

    @Test
    fun testType_noPriceNoDirection() {
        val priceAlert = createPriceAlert(
            price = null,
            priceDirection = null,
        )
        val aggregate = createAggregate(priceAlert = priceAlert)

        assertEquals(PriceAlertType.Auto, aggregate.type)
    }

    @Test
    fun testType_directionOverridesPrice() {
        val assetPrice = createAssetPriceInfo(price = 45000.0)
        val priceAlert = createPriceAlert(
            price = 50000.0,
            priceDirection = PriceAlertDirection.Down,
        )
        val aggregate = createAggregate(
            assetPrice = assetPrice,
            priceAlert = priceAlert,
        )

        assertEquals(PriceAlertType.Under, aggregate.type)
    }

    @Test
    fun testMultipleAssets() {
        val btcAggregate = createAggregate(
            id = 1,
            asset = btcAsset,
            assetPrice = createAssetPriceInfo(btcAsset.id, 45000.0),
        )
        val ethAggregate = createAggregate(
            id = 2,
            asset = ethAsset,
            assetPrice = createAssetPriceInfo(ethAsset.id, 2500.0),
        )
        val solAggregate = createAggregate(
            id = 3,
            asset = solAsset,
            assetPrice = createAssetPriceInfo(solAsset.id, 98.5),
        )

        assertEquals(1, btcAggregate.id)
        assertEquals(2, ethAggregate.id)
        assertEquals(3, solAggregate.id)
        assertEquals("Bitcoin", btcAggregate.title)
        assertEquals("Ethereum", ethAggregate.title)
        assertEquals("Solana", solAggregate.title)
    }
}