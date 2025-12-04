package com.gemwallet.android.data.coordinates.perpetuals

import com.gemwallet.android.data.repositoreis.perpetual.FakePerpetualRepository
import com.gemwallet.android.domains.price.PriceState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PerpetualPositionDataAggregateTest {

    private lateinit var repository: FakePerpetualRepository

    @Before
    fun setup() {
        repository = FakePerpetualRepository()
    }

    @Test
    fun testPerpetualPositionDataAggregate_basicProperties() = runBlocking {
        val positions = repository.getPositions().first()
        val btcPosition = positions[0]
        val aggregate = PerpetualPositionDataAggregateImpl(btcPosition)

        assertEquals("pos-btc-1", aggregate.positionId)
        assertEquals("BTC-PERP", aggregate.perpetualId)
        assertEquals("Bitcoin", aggregate.asset.name)
        assertEquals("BTC", aggregate.asset.symbol)
        assertEquals("Bitcoin Perpetual", aggregate.name)
    }

    @Test
    fun testPerpetualPositionDataAggregate_marginAmount_formatting() = runBlocking {
        val positions = repository.getPositions().first()
        val btcPosition = positions[0]
        val aggregate = PerpetualPositionDataAggregateImpl(btcPosition)

        assertTrue(aggregate.marginAmount.contains("4,771"))
    }

    @Test
    fun testPerpetualPositionDataAggregate_pnlWithPercentage() = runBlocking {
        val positions = repository.getPositions().first()
        assertEquals("+\$460.25 (+9.64%)", PerpetualPositionDataAggregateImpl(positions[0]).pnlWithPercentage)
        assertEquals("-\$121.25 (-3.34%)", PerpetualPositionDataAggregateImpl(positions[1]).pnlWithPercentage)
    }

    @Test
    fun testPerpetualPositionDataAggregate_pnlState() = runBlocking {
        val positions = repository.getPositions().first()
        assertEquals(PriceState.Up, PerpetualPositionDataAggregateImpl(positions[0]).pnlState)
        assertEquals(PriceState.Down, PerpetualPositionDataAggregateImpl(positions[1]).pnlState)
    }
}
