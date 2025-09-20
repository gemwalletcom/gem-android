package com.gemwallet.android.blockchain.clients.cosmos

import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.CosmosDenom

fun CosmosDenom.Companion.from(chain: Chain): String = when (chain) {
    Chain.Cosmos -> CosmosDenom.Uatom.string
    Chain.Osmosis -> CosmosDenom.Uosmo.string
    Chain.Thorchain -> CosmosDenom.Rune.string
    Chain.Celestia -> CosmosDenom.Utia.string
    Chain.Injective -> CosmosDenom.Inj.string
    Chain.Sei -> CosmosDenom.Usei.string
    Chain.Noble -> CosmosDenom.Uusdc.string
    else -> throw IllegalArgumentException("Coin is not supported")
}