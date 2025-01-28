package com.gemwallet.features.nft.presents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.components.image.AsyncImage
import com.gemwallet.android.ui.models.NftItemUIModel
import com.gemwallet.android.ui.models.actions.NftAssetIdAction
import com.gemwallet.android.ui.models.actions.NftCollectionIdAction

@Composable
fun NFTItem(
    model: NftItemUIModel,
    collectionIdAction: NftCollectionIdAction,
    assetIdAction: NftAssetIdAction,
) {
    Column(modifier = Modifier.clickable(onClick = { model.onClick(collectionIdAction, assetIdAction) })) {
        AsyncImage(
            model.imageUrl,
            placeholderText = model.name,
            modifier = Modifier.size(150.dp)
        )
        Text(model.name)
    }
}