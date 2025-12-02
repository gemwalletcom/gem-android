package com.gemwallet.android.data.coordinates.perpetuals

import com.gemwallet.android.data.repositoreis.perpetual.FakePerpetualRepository
import com.gemwallet.android.domains.price.PriceState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetPerpetualsImplTest {

    private lateinit var repository: FakePerpetualRepository
    private lateinit var getPerpetuals: GetPerpetualsImpl

    @Before
    fun setup() {
        repository = FakePerpetualRepository()
        getPerpetuals = GetPerpetualsImpl(repository)
    }

    @Test
    fun testGetPerpetuals_returnsAllItems() = runBlocking {
        val result = getPerpetuals.getPerpetuals(null).first()

        assertEquals(15, result.size)
    }

    @Test
    fun testGetPerpetuals_searchBySymbol_BTC() = runBlocking {
        val result = getPerpetuals.getPerpetuals("BTC").first()

        assertEquals(1, result.size)
        assertEquals("BTC-PERP", result[0].id)
        assertEquals("Bitcoin Perpetual", result[0].name)
        assertEquals("BTC", result[0].asset.symbol)
    }

    @Test
    fun testGetPerpetuals_searchByName_Bitcoin() = runBlocking {
        val result = getPerpetuals.getPerpetuals("Bitcoin").first()

        assertEquals(1, result.size)
        assertEquals("BTC-PERP", result[0].id)
        assertEquals("Bitcoin Perpetual", result[0].name)
    }

    @Test
    fun testGetPerpetuals_searchNotFound() = runBlocking {
        val result = getPerpetuals.getPerpetuals("NOTEXIST").first()

        assertEquals(0, result.size)
    }

    @Test
    fun testGetPerpetuals_Formatting() = runBlocking {
        val result = getPerpetuals.getPerpetuals(null).first()
        val item = result[0]
        assertEquals("\$95,420.50", item.price.priceValueFormated)
        assertEquals("+2.50%", item.price.dayChangePercentageFormatted)
        assertEquals(PriceState.Up, item.price.state)
        assertEquals("\$15.00B", item.volume)
    }
}
