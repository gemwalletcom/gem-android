package com.gemwallet.android.features.widgets

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.BitmapImageProvider
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextDefaults.defaultTextStyle
import androidx.glance.unit.ColorProvider
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import com.gemwallet.android.data.repositoreis.di.WidgetEntryPoint
import com.gemwallet.android.domains.asset.getIconUrl
import com.gemwallet.android.domains.percentage.formatAsPercentage
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.format
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.theme.paddingDefault
import com.gemwallet.android.ui.theme.paddingHalfSmall
import com.gemwallet.android.ui.theme.paddingSmall
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class PricesWidget() : GlanceAppWidget() {

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val assetsRepository = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java).assetsRepository()
        val noData = context.getString(R.string.errors_no_data_available)
        val assets = try {
            (assetsRepository.getTokensInfo(
                listOf(
                    AssetId(Chain.Bitcoin).toIdentifier(),
                    AssetId(Chain.Ethereum).toIdentifier(),
                    AssetId(Chain.Solana).toIdentifier(),
                )
            ).firstOrNull() ?: emptyList()).reversed()
        } catch (_: Throwable) {
            emptyList()
        }

        provideContent {
            GlanceTheme {
                if (assets.isNotEmpty()) {
                    Assets(assets)
                } else {
                    Box(
                        modifier = GlanceModifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                    ) {
                        Text(
                            modifier = GlanceModifier.fillMaxSize().padding(paddingDefault),
                            text = noData,
                            style = defaultTextStyle.copy(textAlign = TextAlign.Center),
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun Assets(assets: List<AssetInfo>) {
        Column(
            modifier = GlanceModifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize(),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            assets.forEach {
                AssetItem(it)
            }
        }
    }

    companion object {
        init {
            System.loadLibrary("TrustWalletCore")
            System.loadLibrary("gemstone")
        }
    }
}

@Composable
private fun AssetItem(asset: AssetInfo) {
    val context = LocalContext.current
    val imageUrl = asset.id().getIconUrl()
    var loadedBitmap by remember() { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(imageUrl) {
        withContext(Dispatchers.IO) {
            val request = ImageRequest.Builder(context).data(imageUrl).apply {
                memoryCachePolicy(CachePolicy.DISABLED)
                diskCachePolicy(CachePolicy.DISABLED)
            }.build()

            // Request the image to be loaded and return null if an error has occurred
            loadedBitmap = when (val result = context.imageLoader.execute(request)) {
                is ErrorResult -> null
                is SuccessResult -> result.image.toBitmap()
            }
        }
    }
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = paddingDefault, vertical = paddingSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box() {
            loadedBitmap?.let {
                Image(
                    BitmapImageProvider(it),
                    contentDescription = ""
                )
            }
        }
        Spacer(GlanceModifier.size(paddingDefault))
        Column(
            modifier = GlanceModifier.defaultWeight()
        ) {
            WidgetTitleText(asset.asset.name)
            Spacer(GlanceModifier.size(paddingHalfSmall))
            WidgetSubtitleText(asset.asset.symbol)
        }
        asset.price?.let {
            val percentageColor = when {
                it.price.priceChangePercentage24h < 0 -> Color(0xFFF84E4E)
                it.price.priceChangePercentage24h > 0 -> Color(0xFF06BE92)
                else -> Color(0xFF808d99)
            }
            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.End,
            ) {
                WidgetTitleText(it.currency.format(it.price.price))
                Spacer(GlanceModifier.size(paddingHalfSmall))
                WidgetSubtitleText(it.price.priceChangePercentage24h.formatAsPercentage(), percentageColor)
            }
        }
    }
}

@Composable
private fun WidgetTitleText(text: String) {
    Text(
        modifier = GlanceModifier,
        text = text,
        maxLines = 1,
        style = defaultTextStyle.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium),
    )
}

@Composable
private fun WidgetSubtitleText(text: String, color: Color = Color.Black) {
    Text(
        modifier = GlanceModifier,
        text = text,
        maxLines = 1,
        style = defaultTextStyle.copy(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = ColorProvider(color)),
    )
}