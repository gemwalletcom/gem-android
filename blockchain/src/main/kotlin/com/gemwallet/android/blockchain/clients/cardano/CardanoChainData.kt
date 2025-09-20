package com.gemwallet.android.blockchain.clients.cardano

import com.gemwallet.android.blockchain.services.mapper.toUtxo
import com.gemwallet.android.model.ChainSignData
import com.wallet.core.primitives.UTXO
import uniffi.gemstone.GemTransactionLoadMetadata

data class CardanoChainData(
    val utxos: List<UTXO>,
) : ChainSignData

fun GemTransactionLoadMetadata.Cardano.toChainData(): CardanoChainData {
    return CardanoChainData(
        utxos = utxos.toUtxo(),
    )
}