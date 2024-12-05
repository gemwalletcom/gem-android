package com.gemwallet.android.ui.components.qr_code

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Composable
fun rememberQRCodePainter(
    content: String,
    cacheName: String,
    size: Dp = 150.dp,
    padding: Dp = 0.dp,
): BitmapPainter {
    val density = LocalDensity.current
    val sizePx = with(density) { size.roundToPx() }
    val paddingPx = with(density) { padding.roundToPx() }
    val context = LocalContext.current
    val generator = QRCodeGenerator()

    var bitmap by remember(cacheName) {
        mutableStateOf<Bitmap?>(null)
    }

    LaunchedEffect(bitmap) {
        if (bitmap != null) return@LaunchedEffect

        launch(Dispatchers.IO) {
            val qrFile = File(context.cacheDir, "qr_code_with_logo_${cacheName}.png")
            bitmap = if (qrFile.exists()) {
                BitmapFactory.decodeFile(qrFile.absolutePath)
            } else {
                val newBitmap = generator.generate(
                    context = context,
                    content = content,
                    size = sizePx,
                    logoAssetName = "brandmark.png",
                    padding = paddingPx
                )
                qrFile.createNewFile()
                FileOutputStream(qrFile).use {
                    newBitmap?.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
                newBitmap
            }
        }
    }
    return remember(bitmap) {
        val currentBitmap = bitmap ?: Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            .apply {
                eraseColor(Color.TRANSPARENT)
            }
        BitmapPainter(currentBitmap.asImageBitmap())
    }
}