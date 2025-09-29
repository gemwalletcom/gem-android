package com.gemwallet.android.blockchain.services.mapper

import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.UTXO
import uniffi.gemstone.GemAsset
import uniffi.gemstone.GemAssetType
import uniffi.gemstone.GemUtxo

fun Asset.toGem() = GemAsset(
    id = id.toIdentifier(),
    chain = id.chain.string,
    tokenId = id.tokenId,
    name = name,
    symbol = symbol,
    decimals = decimals,
    assetType = when (type) {
        AssetType.NATIVE -> GemAssetType.NATIVE
        AssetType.ERC20 -> GemAssetType.ERC20
        AssetType.BEP20 -> GemAssetType.BEP20
        AssetType.SPL -> GemAssetType.SPL
        AssetType.SPL2022 -> GemAssetType.SPL2022
        AssetType.TRC20 -> GemAssetType.TRC20
        AssetType.TOKEN -> GemAssetType.TOKEN
        AssetType.IBC -> GemAssetType.IBC
        AssetType.JETTON -> GemAssetType.JETTON
        AssetType.SYNTH -> GemAssetType.SYNTH
        AssetType.ASA -> GemAssetType.ASA
        AssetType.PERPETUAL -> GemAssetType.PERPETUAL
    }
)

fun GemAsset.toApp() = Asset(
    id = id.toAssetId()!!,
    name = name,
    symbol = symbol,
    decimals = decimals,
    type = when (assetType) {
        GemAssetType.NATIVE -> AssetType.NATIVE
        GemAssetType.ERC20 -> AssetType.ERC20
        GemAssetType.BEP20 -> AssetType.BEP20
        GemAssetType.SPL -> AssetType.SPL
        GemAssetType.SPL2022 -> AssetType.SPL2022
        GemAssetType.TRC20 -> AssetType.TRC20
        GemAssetType.TOKEN -> AssetType.TOKEN
        GemAssetType.IBC -> AssetType.IBC
        GemAssetType.JETTON -> AssetType.JETTON
        GemAssetType.SYNTH -> AssetType.SYNTH
        GemAssetType.ASA -> AssetType.ASA
        GemAssetType.PERPETUAL -> AssetType.PERPETUAL
    }
)

fun GemUtxo.toUtxo(): UTXO = UTXO(
    transactionId,
    vout.toInt(),
    value,
    address
)

fun List<GemUtxo>.toUtxo() = map { it.toUtxo() }