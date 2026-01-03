package com.gemwallet.android.data.coordinates.perpetuals

import com.gemwallet.android.application.perpetual.coordinators.GetPerpetualPositions
import com.gemwallet.android.data.repositoreis.perpetual.FakePerpetualRepository
import com.gemwallet.android.domains.price.PriceState
import com.wallet.core.primitives.PerpetualDirection
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetPerpetualPositionsTest {

    private lateinit var repository: FakePerpetualRepository
    private lateinit var getPerpetualPositions: GetPerpetualPositions

    @Before
    fun setup() {
        repository = FakePerpetualRepository()
        getPerpetualPositions = GetPerpetualPositionsImpl(repository)
    }

    @Test
    fun testGetPerpetualPositions_returnsAllPositions() = runBlocking {
        val result = getPerpetualPositions.getPerpetualPositions().first()

        assertEquals(2, result.size)
    }

    @Test
    fun testGetPerpetualPositions_btcPosition() = runBlocking {
        val result = getPerpetualPositions.getPerpetualPositions().first()
        val btcPosition = result[0]

        assertEquals("pos-btc-1", btcPosition.positionId)
        assertEquals("BTC-PERP", btcPosition.perpetualId)
        assertEquals("Bitcoin", btcPosition.asset.name)
        assertEquals("BTC", btcPosition.asset.symbol)
        assertEquals("Bitcoin Perpetual", btcPosition.name)
    }

    @Test
    fun testGetPerpetualPositions_directions() = runBlocking {
        val result = getPerpetualPositions.getPerpetualPositions().first()

        assertEquals(PerpetualDirection.Long, result[0].direction)
        assertEquals(PerpetualDirection.Short, result[1].direction)
    }

    @Test
    fun testGetPerpetualPositions_leverage() = runBlocking {
        val result = getPerpetualPositions.getPerpetualPositions().first()

        assertEquals(10, result[0].leverage)
        assertEquals(5, result[1].leverage)
    }

    @Test
    fun testGetPerpetualPositions_marginAmountFormatting() = runBlocking {
        val result = getPerpetualPositions.getPerpetualPositions().first()

        assertTrue(result[0].marginAmount.contains("4,771"))
        assertTrue(result[1].marginAmount.contains("3,625"))
    }

    @Test
    fun testGetPerpetualPositions_pnlWithPercentage() = runBlocking {
        val result = getPerpetualPositions.getPerpetualPositions().first()

        assertEquals("+\$460.25 (+9.64%)", result[0].pnlWithPercentage)
        assertEquals("-\$121.25 (-3.34%)", result[1].pnlWithPercentage)
    }

    @Test
    fun testGetPerpetualPositions_pnlState() = runBlocking {
        val result = getPerpetualPositions.getPerpetualPositions().first()

        assertEquals(PriceState.Up, result[0].pnlState)
        assertEquals(PriceState.Down, result[1].pnlState)
    }
}
