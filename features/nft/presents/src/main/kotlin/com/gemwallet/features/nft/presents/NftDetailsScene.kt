package com.gemwallet.features.nft.presents

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.cells.propertyNetwork
import com.gemwallet.android.ui.components.designsystem.listSpacerBig
import com.gemwallet.android.ui.components.image.AsyncImage
import com.gemwallet.android.ui.components.list_item.PropertyItem
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.actions.CancelAction
import com.gemwallet.features.nft.viewmodels.NftAssetDetailsUIModel
import com.gemwallet.features.nft.viewmodels.NftDetailsViewModel
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetLink
import com.wallet.core.primitives.NFTAttribute

@Composable
fun NFTDetailsScene(
    cancelAction: CancelAction,
    onRecipient: (AssetId, String) -> Unit,
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
        actions = {
            IconButton( { onRecipient(AssetId(model.asset.chain), model.asset.id) } ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "Send nft")
            }
        },
        onClose = { cancelAction() },
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                AsyncImage(
                    model.imageUrl,
                    size = null,
                    transformation = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                )
            }
            listSpacerBig()
            generalInfo(model)
            nftAttributes(model.attributes)
            nftLinks(model.collection.links) { uriHandler.openUri(it) }
        }
    }
}

private fun LazyListScope.generalInfo(model: NftAssetDetailsUIModel) {
    item {
        PropertyItem(R.string.nft_collection, model.collection.name)
        propertyNetwork(model.collection.chain)
        model.asset.contractAddress?.let {
            PropertyItem(R.string.asset_contract, it)
        }
        PropertyItem(R.string.asset_token_id, model.asset.tokenId)
    }
}

private fun LazyListScope.nftAttributes(attributes: List<NFTAttribute>) {
    item {
        SubheaderItem(stringResource(R.string.nft_properties))
    }
    items(attributes) {
        PropertyItem(it.name, it.value)
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
        links.firstOrNull { it.name == "website"}?.let {
            PropertyItem(R.string.social_website, R.drawable.website) { onLinkClick(it.url) }
        }

        links.filter { it.name != "website" }.sortedByDescending { it.name }.map {
            val (url, title, icon) = when (it.name) {
                "coingecko" -> Triple(it.url, R.string.social_coingecko, R.drawable.coingecko)
                "x", "twitter" -> Triple(it.url, R.string.social_x, R.drawable.twitter)
                "telegram" -> Triple(it.url, R.string.social_telegram, R.drawable.telegram)
                "github" -> Triple(it.url, R.string.social_github, R.drawable.github)
                "instagram" -> Triple(it.url, R.string.social_instagram, R.drawable.instagram)
                "opensea" -> Triple(it.url, R.string.social_opensea, R.drawable.opensea)
                "magiceden" -> Triple(it.url, R.string.social_magiceden, R.drawable.magiceden)
                "coinmarketcap" -> Triple(it.url, R.string.social_coinmarketcap, R.drawable.coinmarketcap)
                "tiktok" -> Triple(it.url, R.string.social_tiktok, R.drawable.tiktok)
                "discord" -> Triple(it.url, R.string.social_discord, R.drawable.discord)
                else -> Triple(it.url, R.string.social_website, R.drawable.website)
            }
            PropertyItem(title, icon) { onLinkClick(url) }
        }
    }
}

