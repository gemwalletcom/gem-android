package com.wallet

import com.gemwallet.android.ext.asset
import com.wallet.core.primitives.Chain
import org.junit.Assert.assertEquals
import org.junit.Test

class TestChainAssets {

    @Test
    fun testDecimals() {
        for (chain in Chain.entries) {
            when (chain) {
                Chain.Bitcoin -> assertEquals(chain.asset().decimals, 8)
                Chain.Litecoin -> assertEquals(chain.asset().decimals, 8)
                Chain.Ethereum -> assertEquals(chain.asset().decimals, 18)
                Chain.SmartChain -> assertEquals(chain.asset().decimals, 18)
                Chain.Solana -> assertEquals(chain.asset().decimals, 9)
                Chain.Polygon -> assertEquals(chain.asset().decimals, 18)
                Chain.Thorchain -> assertEquals(chain.asset().decimals, 8)
                Chain.Cosmos -> assertEquals(chain.asset().decimals, 6)
                Chain.Osmosis -> assertEquals(chain.asset().decimals, 6)
                Chain.Arbitrum -> assertEquals(chain.asset().decimals, 18)
                Chain.Ton -> assertEquals(chain.asset().decimals, 9)
                Chain.Tron -> assertEquals(chain.asset().decimals, 6)
                Chain.Doge -> assertEquals(chain.asset().decimals, 8)
                Chain.Optimism -> assertEquals(chain.asset().decimals, 18)
                Chain.Aptos -> assertEquals(chain.asset().decimals, 8)
                Chain.Base -> assertEquals(chain.asset().decimals, 18)
                Chain.AvalancheC -> assertEquals(chain.asset().decimals, 18)
                Chain.Sui -> assertEquals(chain.asset().decimals, 9)
                Chain.Xrp -> assertEquals(chain.asset().decimals, 6)
                Chain.OpBNB -> assertEquals(chain.asset().decimals, 18)
                Chain.Fantom -> assertEquals(chain.asset().decimals, 18)
                Chain.Gnosis -> assertEquals(chain.asset().decimals, 18)
                Chain.Celestia -> assertEquals(chain.asset().decimals, 6)
                Chain.Injective -> assertEquals(chain.asset().decimals, 18)
                Chain.Sei -> assertEquals(chain.asset().decimals, 6)
                Chain.Manta -> assertEquals(chain.asset().decimals, 18)
                Chain.Blast -> assertEquals(chain.asset().decimals, 18)
                Chain.Noble -> assertEquals(chain.asset().decimals, 6)
                Chain.ZkSync -> assertEquals(chain.asset().decimals, 18)
                Chain.Linea -> assertEquals(chain.asset().decimals, 18)
                Chain.Mantle -> assertEquals(chain.asset().decimals, 18)
                Chain.Celo -> assertEquals(chain.asset().decimals, 18)
                Chain.Near -> assertEquals(chain.asset().decimals, 24)
            }
        }
    }

    @Test
    fun testSymbol() {
        for (chain in Chain.entries) {
            when (chain) {
                Chain.Bitcoin -> assertEquals(chain.asset().symbol, "BTC")
                Chain.Litecoin -> assertEquals(chain.asset().symbol, "LTC")
                Chain.Ethereum -> assertEquals(chain.asset().symbol, "ETH")
                Chain.SmartChain -> assertEquals(chain.asset().symbol, "BNB")
                Chain.Solana -> assertEquals(chain.asset().symbol, "SOL")
                Chain.Polygon -> assertEquals(chain.asset().symbol, "MATIC")
                Chain.Thorchain -> assertEquals(chain.asset().symbol, "RUNE")
                Chain.Cosmos -> assertEquals(chain.asset().symbol, "ATOM")
                Chain.Osmosis -> assertEquals(chain.asset().symbol, "OSMO")
                Chain.Arbitrum -> assertEquals(chain.asset().symbol, "ETH")
                Chain.Ton -> assertEquals(chain.asset().symbol, "TON")
                Chain.Tron -> assertEquals(chain.asset().symbol, "TRX")
                Chain.Doge -> assertEquals(chain.asset().symbol, "DOGE")
                Chain.Optimism -> assertEquals(chain.asset().symbol, "ETH")
                Chain.Aptos -> assertEquals(chain.asset().symbol, "APT")
                Chain.Base -> assertEquals(chain.asset().symbol, "ETH")
                Chain.AvalancheC -> assertEquals(chain.asset().symbol, "AVAX")
                Chain.Sui -> assertEquals(chain.asset().symbol, "SUI")
                Chain.Xrp -> assertEquals(chain.asset().symbol, "XRP")
                Chain.OpBNB -> assertEquals(chain.asset().symbol, "BNB")
                Chain.Fantom -> assertEquals(chain.asset().symbol, "FTM")
                Chain.Gnosis -> assertEquals(chain.asset().symbol, "GNO")
                Chain.Celestia -> assertEquals(chain.asset().symbol, "TIA")
                Chain.Injective -> assertEquals(chain.asset().symbol, "INJ")
                Chain.Sei -> assertEquals(chain.asset().symbol, "SEI")
                Chain.Manta -> assertEquals(chain.asset().symbol, "ETH")
                Chain.Noble -> assertEquals(chain.asset().symbol, "USDC")
                Chain.Blast -> assertEquals(chain.asset().symbol, "ETH")
                Chain.ZkSync -> assertEquals(chain.asset().symbol, "ETH")
                Chain.Linea -> assertEquals(chain.asset().symbol, "ETH")
                Chain.Mantle -> assertEquals(chain.asset().symbol, "MNT")
                Chain.Celo -> assertEquals(chain.asset().symbol, "CELO")
                Chain.Near -> assertEquals(chain.asset().symbol, "NEAR")
            }
        }
    }
}