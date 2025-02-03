package com.gemwallet.features.nft.presents

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.components.cells.cellNetwork
import com.gemwallet.android.ui.components.designsystem.listSpacerBig
import com.gemwallet.android.ui.components.image.AsyncImage
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.actions.CancelAction
import com.gemwallet.features.nft.viewmodels.NftAssetDetailsUIModel
import com.gemwallet.features.nft.viewmodels.NftDetailsViewModel
import com.wallet.core.primitives.AssetLink
import com.wallet.core.primitives.NFTAttribute

@Composable
fun NFTDetailsScene(
    cancelAction: CancelAction,
) {
    val viewModel: NftDetailsViewModel = hiltViewModel()
    val assetData by viewModel.nftAsset.collectAsStateWithLifecycle()

    val uriHandler = LocalUriHandler.current

    if (assetData == null) {
        return
    }
    val model = assetData!!
    Scene(
        title = model.assetName,
        onClose = { cancelAction() }
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                AsyncImage(
                    model.imageUrl,
                    size = null,
                    transformation = null,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                )
            }
            listSpacerBig()
            generalInfo(model)
            nftAttributes(model.attributes)
            nftLinks(model.collection.links, { uriHandler.openUri(it) })
        }
    }
}

private fun LazyListScope.generalInfo(model: NftAssetDetailsUIModel) {
    item {
        Table(
            listOf(
                CellEntity(
                    label = R.string.nft_collection,
                    data = model.collection.name,
                ),
                cellNetwork(model.collection.chain),
                model.asset.contractAddress?.let {
                    CellEntity(
                        label = R.string.asset_contract,
                        data = it,
                    )
                },
                CellEntity(
                    label = R.string.asset_token_id,
                    data = model.asset.tokenId,
                )
            )
        )
    }
}

private fun LazyListScope.nftAttributes(attributes: List<NFTAttribute>) {
    item {
        SubheaderItem(stringResource(R.string.nft_properties))
    }

    item {
        Table(
            attributes.map {
                CellEntity(
                    label = it.name,
                    data = it.value,
                )
            }
        )
    }
}

private fun LazyListScope.nftLinks(links: List<AssetLink>, onLinkClick: (String) -> Unit) {
    if (links.isEmpty()) {
        return
    }
    item {
        SubheaderItem(title = stringResource(R.string.social_links))
    }

    item {
        Table(
            links.map {
                CellEntity(
                    label = it.name,
                    data = "",
                    action = { onLinkClick(it.url) }
                )
            }
        )
    }
}

