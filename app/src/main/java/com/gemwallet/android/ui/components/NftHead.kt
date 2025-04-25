package com.gemwallet.android.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.transform.RoundedCornersTransformation
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.designsystem.padding16
import com.gemwallet.android.ui.components.image.AsyncImage
import com.wallet.core.primitives.NFTAsset

@Composable
fun NftHead(
    nftAsset: NFTAsset
) {
    Column {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = padding16, end = padding16, bottom = padding16),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AsyncImage(
                size = 128.dp,
                model = nftAsset.images.preview.url,
                placeholderText = nftAsset.name,
                transformation = RoundedCornersTransformation(32f, 32f, 32f, 32f),
                contentDescription = "header_icon",
            )
            Spacer16()
            DisplayText(text = nftAsset.name, modifier = Modifier.fillMaxWidth())
        }
        Spacer(modifier = Modifier.size(0.dp))
        HorizontalDivider(thickness = 0.4.dp)
    }
}