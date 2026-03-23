package com.gemwallet.android.blockchain.clients.cardano

import com.gemwallet.android.domains.asset.toGem
import com.gemwallet.android.domains.asset.toUtxo
import com.gemwallet.android.model.ChainSignData
import com.wallet.core.primitives.UTXO
import uniffi.gemstone.GemTransactionLoadMetadata

data class CardanoChainData(
    val utxos: List<UTXO>,
) : ChainSignData {
    override fun toDto(): GemTransactionLoadMetadata {
        return GemTransactionLoadMetadata.Cardano(
            utxos.toGem()
        )
    }
}

fun GemTransactionLoadMetadata.Cardano.toChainData(): CardanoChainData {
    return CardanoChainData(
        utxos = utxos.toUtxo(),
    )
}