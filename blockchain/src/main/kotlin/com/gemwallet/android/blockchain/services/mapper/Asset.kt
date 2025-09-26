package com.gemwallet.android.blockchain.services.mapper

import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.UTXO
import uniffi.gemstone.GemAsset
import uniffi.gemstone.GemUtxo

fun Asset.toGem() = GemAsset(
    id = id.toIdentifier(),
    name = name,
    symbol = symbol,
    decimals = decimals,
    assetType = type.string,
)

fun GemAsset.toApp() = Asset(
    id = id.toAssetId()!!,
    name = name,
    symbol = symbol,
    decimals = decimals,
    type = AssetType.entries.firstOrNull { it.string == assetType }!!
)

fun GemUtxo.toUtxo(): UTXO = UTXO(
    transactionId,
    vout.toInt(),
    value,
    address
)

fun List<GemUtxo>.toUtxo() = map { it.toUtxo() }