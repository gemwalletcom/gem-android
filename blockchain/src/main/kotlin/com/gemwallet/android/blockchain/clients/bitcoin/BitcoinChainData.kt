package com.gemwallet.android.blockchain.clients.bitcoin

import com.gemwallet.android.blockchain.services.mapper.toUtxo
import com.gemwallet.android.model.ChainSignData
import com.wallet.core.primitives.UTXO
import uniffi.gemstone.GemTransactionLoadMetadata

data class BitcoinChainData(
    val utxo: List<UTXO>,
) : ChainSignData

data class ZCashChainData(
    val utxo: List<UTXO>,
    val branchId: String,
) : ChainSignData

fun GemTransactionLoadMetadata.Bitcoin.toChainData(): BitcoinChainData {
    return BitcoinChainData(
        utxo = utxos.toUtxo(),
    )
}

fun GemTransactionLoadMetadata.Zcash.toChainData(): ZCashChainData {
    return ZCashChainData(
        utxo = utxos.toUtxo(),
        branchId = branchId,
    )
}
