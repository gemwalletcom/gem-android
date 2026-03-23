package com.gemwallet.android.blockchain.clients.bitcoin

import com.gemwallet.android.domains.asset.toGem
import com.gemwallet.android.domains.asset.toUtxo
import com.gemwallet.android.model.ChainSignData
import com.wallet.core.primitives.UTXO
import uniffi.gemstone.GemTransactionLoadMetadata

data class BitcoinChainData(
    val utxo: List<UTXO>,
) : ChainSignData {
    override fun toDto(): GemTransactionLoadMetadata {
        return GemTransactionLoadMetadata.Bitcoin(
            utxo.toGem()
        )
    }
}

data class ZCashChainData(
    val utxo: List<UTXO>,
    val branchId: String,
) : ChainSignData {
    override fun toDto(): GemTransactionLoadMetadata {
        return GemTransactionLoadMetadata.Bitcoin(
            utxo.toGem()
        )
    }
}

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
