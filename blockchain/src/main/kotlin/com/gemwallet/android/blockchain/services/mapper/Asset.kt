package com.gemwallet.android.blockchain.services.mapper

import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetType
import uniffi.gemstone.GemAsset

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