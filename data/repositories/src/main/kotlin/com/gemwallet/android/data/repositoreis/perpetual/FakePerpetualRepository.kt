package com.gemwallet.android.data.repositoreis.perpetual

import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChartCandleStick
import com.wallet.core.primitives.Perpetual
import com.wallet.core.primitives.PerpetualBalance
import com.wallet.core.primitives.PerpetualData
import com.wallet.core.primitives.PerpetualMetadata
import com.wallet.core.primitives.PerpetualPosition
import com.wallet.core.primitives.PerpetualPositionData
import com.wallet.core.primitives.PerpetualProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakePerpetualRepository @Inject constructor() : PerpetualRepository {

    private val perpetualsFlow = MutableStateFlow<List<PerpetualData>>(getSamplePerpetuals())
    private val chartDataFlow = MutableStateFlow<Map<String, List<ChartCandleStick>>>(getSampleChartData())
    private val positionsFlow = MutableStateFlow<Map<String, List<PerpetualPositionData>>>(emptyMap())
    private val balancesFlow = MutableStateFlow<Map<String, PerpetualBalance>>(emptyMap())

    override suspend fun putPerpetuals(items: List<PerpetualData>) {
        perpetualsFlow.value = items
    }

    override fun getPerpetuals(query: String?): Flow<List<PerpetualData>> {
        return if (query.isNullOrEmpty()) {
            perpetualsFlow
        } else {
            perpetualsFlow.map { perpetuals ->
                perpetuals.filter {
                    it.asset.symbol.contains(query, ignoreCase = true) ||
                    it.perpetual.name.contains(query, ignoreCase = true)
                }
            }
        }
    }

    override fun getPerpetual(perpetualId: String): Flow<PerpetualData?> {
        return perpetualsFlow.map { perpetuals ->
            perpetuals.firstOrNull { it.perpetual.id == perpetualId }
        }
    }

    override suspend fun putPerpetualChartData(data: List<ChartCandleStick>) {
        if (data.isEmpty()) return

        val perpetualId = perpetualsFlow.value.firstOrNull()?.perpetual?.id ?: return
        val currentData = chartDataFlow.value.toMutableMap()
        currentData[perpetualId] = data
        chartDataFlow.value = currentData
    }

    override fun getPerpetualChartData(perpetualId: String): Flow<List<ChartCandleStick>> {
        return chartDataFlow.map { chartMap ->
            chartMap[perpetualId] ?: emptyList()
        }
    }

    override suspend fun removeNotAvailablePositions(accountAddress: String, items: List<PerpetualPosition>) {
        val currentPositions = positionsFlow.value.toMutableMap()
        val availablePositionIds = items.map { it.id }.toSet()
        currentPositions[accountAddress] = currentPositions[accountAddress]?.filter {
            it.position.id in availablePositionIds
        } ?: emptyList()
        positionsFlow.value = currentPositions
    }

    override suspend fun putPositions(accountAddress: String, items: List<PerpetualPosition>) {
        val currentPositions = positionsFlow.value.toMutableMap()
        currentPositions[accountAddress] = items.map { position ->
            val perpetual = perpetualsFlow.value.firstOrNull { it.perpetual.id == position.perpetualId }
            if (perpetual != null) {
                PerpetualPositionData(
                    perpetual = perpetual.perpetual,
                    asset = perpetual.asset,
                    position = position
                )
            } else {
                null
            }
        }.filterNotNull()
        positionsFlow.value = currentPositions
    }

    override fun getPositions(accountAddress: List<String>): Flow<List<PerpetualPositionData>> {
        return positionsFlow.map { positionsMap ->
            accountAddress.flatMap { address ->
                positionsMap[address] ?: emptyList()
            }
        }
    }

    override fun getPositionByPositionId(id: String): Flow<PerpetualPositionData?> {
        return positionsFlow.map { positionsMap ->
            positionsMap.values.flatten().firstOrNull { it.position.id == id }
        }
    }

    override fun getPositionByPerpetualId(id: String): Flow<PerpetualPositionData?> {
        return positionsFlow.map { positionsMap ->
            positionsMap.values.flatten().firstOrNull { it.perpetual.id == id }
        }
    }

    override suspend fun putBalance(accountAddress: String, balance: PerpetualBalance) {
        val currentBalances = balancesFlow.value.toMutableMap()
        currentBalances[accountAddress] = balance
        balancesFlow.value = currentBalances
    }

    override fun getBalances(accountAddresses: List<String>): Flow<List<PerpetualBalance>> {
        return balancesFlow.map { balancesMap ->
            accountAddresses.mapNotNull { address -> balancesMap[address] }
        }
    }

    override suspend fun setMetadata(
        perpetualId: String,
        metadata: PerpetualMetadata
    ) { }

    private fun getSamplePerpetuals(): List<PerpetualData> {
        val btcAsset = Asset(
            id = AssetId(Chain.Bitcoin),
            name = "Bitcoin",
            symbol = "BTC",
            decimals = 8,
            type = AssetType.NATIVE
        )

        val ethAsset = Asset(
            id = AssetId(Chain.Ethereum),
            name = "Ethereum",
            symbol = "ETH",
            decimals = 18,
            type = AssetType.NATIVE
        )

        val solAsset = Asset(
            id = AssetId(Chain.Solana),
            name = "Solana",
            symbol = "SOL",
            decimals = 9,
            type = AssetType.NATIVE
        )

        val bnbAsset = Asset(
            id = AssetId(Chain.SmartChain),
            name = "BNB",
            symbol = "BNB",
            decimals = 18,
            type = AssetType.NATIVE
        )

        val maticAsset = Asset(
            id = AssetId(Chain.Polygon),
            name = "Polygon",
            symbol = "MATIC",
            decimals = 18,
            type = AssetType.NATIVE
        )

        val avaxAsset = Asset(
            id = AssetId(Chain.AvalancheC),
            name = "Avalanche",
            symbol = "AVAX",
            decimals = 18,
            type = AssetType.NATIVE
        )

        val adaAsset = Asset(
            id = AssetId(Chain.Cardano),
            name = "Cardano",
            symbol = "ADA",
            decimals = 6,
            type = AssetType.NATIVE
        )

        val usdtAsset = Asset(
            id = AssetId(Chain.Ethereum, "0xdac17f958d2ee523a2206206994597c13d831ec7"),
            name = "Tether USD",
            symbol = "USDT",
            decimals = 6,
            type = AssetType.ERC20
        )

        val usdcAsset = Asset(
            id = AssetId(Chain.Ethereum, "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"),
            name = "USD Coin",
            symbol = "USDC",
            decimals = 6,
            type = AssetType.ERC20
        )

        val linkAsset = Asset(
            id = AssetId(Chain.Ethereum, "0x514910771af9ca656af840dff83e8264ecf986ca"),
            name = "Chainlink",
            symbol = "LINK",
            decimals = 18,
            type = AssetType.ERC20
        )

        val uniAsset = Asset(
            id = AssetId(Chain.Ethereum, "0x1f9840a85d5af5bf1d1762f925bdaddc4201f984"),
            name = "Uniswap",
            symbol = "UNI",
            decimals = 18,
            type = AssetType.ERC20
        )

        val aaveAsset = Asset(
            id = AssetId(Chain.Ethereum, "0x7fc66500c84a76ad7e9c93437bfc5ac33e2ddae9"),
            name = "Aave",
            symbol = "AAVE",
            decimals = 18,
            type = AssetType.ERC20
        )

        val cakeAsset = Asset(
            id = AssetId(Chain.SmartChain, "0x0e09fabb73bd3ade0a17ecc321fd13a19e81ce82"),
            name = "PancakeSwap",
            symbol = "CAKE",
            decimals = 18,
            type = AssetType.BEP20
        )

        val rayAsset = Asset(
            id = AssetId(Chain.Solana, "4k3Dyjzvzp8eMZWUXbBCjEvwSkkk59S5iCNLY3QrkX6R"),
            name = "Raydium",
            symbol = "RAY",
            decimals = 6,
            type = AssetType.SPL
        )

        val orcaAsset = Asset(
            id = AssetId(Chain.Solana, "orcaEKTdK7LKz57vaAYr9QeNsVEPfiu6QeMU1kektZE"),
            name = "Orca",
            symbol = "ORCA",
            decimals = 6,
            type = AssetType.SPL
        )

        return listOf(
            PerpetualData(
                perpetual = Perpetual(
                    id = "BTC-PERP",
                    name = "Bitcoin Perpetual",
                    provider = PerpetualProvider.Hypercore,
                    assetId = AssetId(Chain.Bitcoin),
                    identifier = "BTC-PERP",
                    price = 95420.50,
                    pricePercentChange24h = 2.5,
                    openInterest = 2500000000.0,
                    volume24h = 15000000000.0,
                    funding = 0.0001,
                    maxLeverage = 100u
                ),
                asset = btcAsset,
                metadata = PerpetualMetadata(isPinned = true)
            ),
            PerpetualData(
                perpetual = Perpetual(
                    id = "ETH-PERP",
                    name = "Ethereum Perpetual",
                    provider = PerpetualProvider.Hypercore,
                    assetId = AssetId(Chain.Ethereum),
                    identifier = "ETH-PERP",
                    price = 3625.75,
                    pricePercentChange24h = 1.8,
                    openInterest = 1200000000.0,
                    volume24h = 8000000000.0,
                    funding = 0.00008,
                    maxLeverage = 50u
                ),
                asset = ethAsset,
                metadata = PerpetualMetadata(isPinned = true)
            ),
            PerpetualData(
                perpetual = Perpetual(
                    id = "SOL-PERP",
                    name = "Solana Perpetual",
                    provider = PerpetualProvider.Hypercore,
                    assetId = AssetId(Chain.Solana),
                    identifier = "SOL-PERP",
                    price = 235.40,
                    pricePercentChange24h = -0.5,
                    openInterest = 500000000.0,
                    volume24h = 3000000000.0,
                    funding = 0.00005,
                    maxLeverage = 20u
                ),
                asset = solAsset,
                metadata = PerpetualMetadata(isPinned = true)
            ),
            PerpetualData(
                perpetual = Perpetual(
                    id = "BNB-PERP",
                    name = "BNB Perpetual",
                    provider = PerpetualProvider.Hypercore,
                    assetId = AssetId(Chain.SmartChain),
                    identifier = "BNB-PERP",
                    price = 612.30,
                    pricePercentChange24h = 3.2,
                    openInterest = 350000000.0,
                    volume24h = 2000000000.0,
                    funding = 0.00012,
                    maxLeverage = 50u
                ),
                asset = bnbAsset,
                metadata = PerpetualMetadata(isPinned = false)
            ),
            PerpetualData(
                perpetual = Perpetual(
                    id = "MATIC-PERP",
                    name = "Polygon Perpetual",
                    provider = PerpetualProvider.Hypercore,
                    assetId = AssetId(Chain.Polygon),
                    identifier = "MATIC-PERP",
                    price = 0.92,
                    pricePercentChange24h = -1.3,
                    openInterest = 120000000.0,
                    volume24h = 800000000.0,
                    funding = 0.00006,
                    maxLeverage = 25u
                ),
                asset = maticAsset,
                metadata = PerpetualMetadata(isPinned = false)
            ),
            PerpetualData(
                perpetual = Perpetual(
                    id = "AVAX-PERP",
                    name = "Avalanche Perpetual",
                    provider = PerpetualProvider.Hypercore,
                    assetId = AssetId(Chain.AvalancheC),
                    identifier = "AVAX-PERP",
                    price = 41.85,
                    pricePercentChange24h = 4.1,
                    openInterest = 200000000.0,
                    volume24h = 1200000000.0,
                    funding = 0.00009,
                    maxLeverage = 30u
                ),
                asset = avaxAsset,
                metadata = PerpetualMetadata(isPinned = false)
            ),
            PerpetualData(
                perpetual = Perpetual(
                    id = "ADA-PERP",
                    name = "Cardano Perpetual",
                    provider = PerpetualProvider.Hypercore,
                    assetId = AssetId(Chain.Cardano),
                    identifier = "ADA-PERP",
                    price = 1.05,
                    pricePercentChange24h = 0.8,
                    openInterest = 180000000.0,
                    volume24h = 950000000.0,
                    funding = 0.00007,
                    maxLeverage = 20u
                ),
                asset = adaAsset,
                metadata = PerpetualMetadata(isPinned = false)
            ),
            PerpetualData(
                perpetual = Perpetual(
                    id = "USDT-PERP",
                    name = "Tether Perpetual",
                    provider = PerpetualProvider.Hypercore,
                    assetId = AssetId(Chain.Ethereum, "0xdac17f958d2ee523a2206206994597c13d831ec7"),
                    identifier = "USDT-PERP",
                    price = 1.0001,
                    pricePercentChange24h = 0.01,
                    openInterest = 500000000.0,
                    volume24h = 3000000000.0,
                    funding = 0.00001,
                    maxLeverage = 10u
                ),
                asset = usdtAsset,
                metadata = PerpetualMetadata(isPinned = false)
            ),
            PerpetualData(
                perpetual = Perpetual(
                    id = "USDC-PERP",
                    name = "USD Coin Perpetual",
                    provider = PerpetualProvider.Hypercore,
                    assetId = AssetId(Chain.Ethereum, "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"),
                    identifier = "USDC-PERP",
                    price = 0.9999,
                    pricePercentChange24h = -0.01,
                    openInterest = 450000000.0,
                    volume24h = 2800000000.0,
                    funding = 0.00001,
                    maxLeverage = 10u
                ),
                asset = usdcAsset,
                metadata = PerpetualMetadata(isPinned = false)
            ),
            PerpetualData(
                perpetual = Perpetual(
                    id = "LINK-PERP",
                    name = "Chainlink Perpetual",
                    provider = PerpetualProvider.Hypercore,
                    assetId = AssetId(Chain.Ethereum, "0x514910771af9ca656af840dff83e8264ecf986ca"),
                    identifier = "LINK-PERP",
                    price = 21.45,
                    pricePercentChange24h = 2.7,
                    openInterest = 150000000.0,
                    volume24h = 900000000.0,
                    funding = 0.00008,
                    maxLeverage = 25u
                ),
                asset = linkAsset,
                metadata = PerpetualMetadata(isPinned = false)
            ),
            PerpetualData(
                perpetual = Perpetual(
                    id = "UNI-PERP",
                    name = "Uniswap Perpetual",
                    provider = PerpetualProvider.Hypercore,
                    assetId = AssetId(Chain.Ethereum, "0x1f9840a85d5af5bf1d1762f925bdaddc4201f984"),
                    identifier = "UNI-PERP",
                    price = 13.85,
                    pricePercentChange24h = 1.2,
                    openInterest = 100000000.0,
                    volume24h = 600000000.0,
                    funding = 0.00007,
                    maxLeverage = 20u
                ),
                asset = uniAsset,
                metadata = PerpetualMetadata(isPinned = false)
            ),
            PerpetualData(
                perpetual = Perpetual(
                    id = "AAVE-PERP",
                    name = "Aave Perpetual",
                    provider = PerpetualProvider.Hypercore,
                    assetId = AssetId(Chain.Ethereum, "0x7fc66500c84a76ad7e9c93437bfc5ac33e2ddae9"),
                    identifier = "AAVE-PERP",
                    price = 320.50,
                    pricePercentChange24h = 3.5,
                    openInterest = 80000000.0,
                    volume24h = 450000000.0,
                    funding = 0.00009,
                    maxLeverage = 20u
                ),
                asset = aaveAsset,
                metadata = PerpetualMetadata(isPinned = false)
            ),
            PerpetualData(
                perpetual = Perpetual(
                    id = "CAKE-PERP",
                    name = "PancakeSwap Perpetual",
                    provider = PerpetualProvider.Hypercore,
                    assetId = AssetId(Chain.SmartChain, "0x0e09fabb73bd3ade0a17ecc321fd13a19e81ce82"),
                    identifier = "CAKE-PERP",
                    price = 3.25,
                    pricePercentChange24h = -0.8,
                    openInterest = 50000000.0,
                    volume24h = 300000000.0,
                    funding = 0.00005,
                    maxLeverage = 15u
                ),
                asset = cakeAsset,
                metadata = PerpetualMetadata(isPinned = false)
            ),
            PerpetualData(
                perpetual = Perpetual(
                    id = "RAY-PERP",
                    name = "Raydium Perpetual",
                    provider = PerpetualProvider.Hypercore,
                    assetId = AssetId(Chain.Solana, "4k3Dyjzvzp8eMZWUXbBCjEvwSkkk59S5iCNLY3QrkX6R"),
                    identifier = "RAY-PERP",
                    price = 2.85,
                    pricePercentChange24h = 1.9,
                    openInterest = 40000000.0,
                    volume24h = 250000000.0,
                    funding = 0.00006,
                    maxLeverage = 15u
                ),
                asset = rayAsset,
                metadata = PerpetualMetadata(isPinned = false)
            ),
            PerpetualData(
                perpetual = Perpetual(
                    id = "ORCA-PERP",
                    name = "Orca Perpetual",
                    provider = PerpetualProvider.Hypercore,
                    assetId = AssetId(Chain.Solana, "orcaEKTdK7LKz57vaAYr9QeNsVEPfiu6QeMU1kektZE"),
                    identifier = "ORCA-PERP",
                    price = 4.15,
                    pricePercentChange24h = -1.5,
                    openInterest = 35000000.0,
                    volume24h = 200000000.0,
                    funding = 0.00004,
                    maxLeverage = 15u
                ),
                asset = orcaAsset,
                metadata = PerpetualMetadata(isPinned = false)
            )
        )
    }

    private fun getSampleChartData(): Map<String, List<ChartCandleStick>> {
        val now = System.currentTimeMillis()
        val hourInMillis = 60 * 60 * 1000L

        val btcChartData = List(24) { index ->
            val basePrice = 95000.0
            val variance = (index % 3 - 1) * 500.0
            ChartCandleStick(
                date = now - (23 - index) * hourInMillis,
                open = basePrice + variance,
                high = basePrice + variance + 300.0,
                low = basePrice + variance - 200.0,
                close = basePrice + variance + 100.0,
                volume = 500000000.0 + (index * 10000000.0)
            )
        }

        val ethChartData = List(24) { index ->
            val basePrice = 3600.0
            val variance = (index % 3 - 1) * 25.0
            ChartCandleStick(
                date = now - (23 - index) * hourInMillis,
                open = basePrice + variance,
                high = basePrice + variance + 15.0,
                low = basePrice + variance - 10.0,
                close = basePrice + variance + 5.0,
                volume = 300000000.0 + (index * 5000000.0)
            )
        }

        val solChartData = List(24) { index ->
            val basePrice = 235.0
            val variance = (index % 3 - 1) * 2.0
            ChartCandleStick(
                date = now - (23 - index) * hourInMillis,
                open = basePrice + variance,
                high = basePrice + variance + 1.5,
                low = basePrice + variance - 1.0,
                close = basePrice + variance + 0.5,
                volume = 100000000.0 + (index * 2000000.0)
            )
        }

        return mapOf(
            "BTC-PERP" to btcChartData,
            "ETH-PERP" to ethChartData,
            "SOL-PERP" to solChartData
        )
    }
}
