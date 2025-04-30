package com.gemwallet.features.nft.presents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.components.designsystem.padding16
import com.gemwallet.android.ui.components.designsystem.padding8
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
    Card(
        modifier = Modifier
            .clickable(onClick = { model.onClick(collectionIdAction, assetIdAction) })
            .padding(start = padding8, bottom = padding16, end = padding8)
        ,
        colors = CardDefaults.cardColors().copy(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AsyncImage(
                model.imageUrl,
                placeholderText = model.name,
                transformation = null,
                size = null,
                modifier = Modifier.widthIn(min = 150.dp).height(150.dp)
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = model.name
            )
        }
    }
}