package com.gemwallet.android.blockchain.clients.bitcoin

import com.gemwallet.android.blockchain.services.mapper.toUtxo
import com.gemwallet.android.model.ChainSignData
import com.wallet.core.primitives.UTXO
import uniffi.gemstone.GemTransactionLoadMetadata

data class BitcoinChainData(
    val utxo: List<UTXO>,
) : ChainSignData

fun GemTransactionLoadMetadata.Bitcoin.toChainData(): BitcoinChainData {
    return BitcoinChainData(
        utxo = utxos.toUtxo()
    )
}