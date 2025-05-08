package com.gemwallet.features.nft.presents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.transform.RoundedCornersTransformation
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
    var imgHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    Card(
        modifier = Modifier
            .clickable(onClick = { model.onClick(collectionIdAction, assetIdAction) })
            .padding(start = padding8, bottom = padding16, end = padding8),
        colors = CardDefaults.cardColors().copy(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AsyncImage(
                model.imageUrl,
                placeholderText = model.name,
                transformation = RoundedCornersTransformation(24f, 24f, 24f, 24f),
                size = null,
                modifier = Modifier
                    .widthIn(min = 150.dp)
                    .height(imgHeight)
                    .onGloballyPositioned { imgHeight = with(density) { it.size.width.toDp() } }
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = model.name,
            )
        }
    }
}