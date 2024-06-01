package com.gemwallet.android.ui

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
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun rememberQRCodePainter(
    content: String,
    cacheName: String,
    size: Dp =  150.dp,
    padding: Dp = 0.dp,
): BitmapPainter {
    val density = LocalDensity.current
    val sizePx = with(density) { size.roundToPx() }
    val paddingPx = with(density) { padding.roundToPx() }
    val context = LocalContext.current

    var bitmap by remember(cacheName) {
        mutableStateOf<Bitmap?>(null)
    }

    LaunchedEffect(bitmap) {
        if (bitmap != null) return@LaunchedEffect

        launch(Dispatchers.IO) {
            val qrFile = File(context.cacheDir, "qr_code_$cacheName.png")
            bitmap = if (qrFile.exists()) {
                BitmapFactory.decodeFile(qrFile.absolutePath)
            } else {
                val newBitmap = generateQr(content, sizePx, paddingPx)
                qrFile.createNewFile()
                FileOutputStream(qrFile).use {
                    newBitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
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

private fun generateQr(
    content: String,
    sizePx: Int,
    paddingPx: Int,
): Bitmap {
    val qrCodeWriter = QRCodeWriter()
    val encodeHints = mutableMapOf<EncodeHintType, Any?>().apply {
        this[EncodeHintType.MARGIN] = paddingPx
    }
    val bitmapMatrix = try {
        qrCodeWriter.encode(
            content,
            BarcodeFormat.QR_CODE,
            sizePx,
            sizePx,
            encodeHints
        )
    } catch (err: Throwable) {
        null
    }
    val matrixWidth = bitmapMatrix?.width ?: 0
    val matrixHeight = bitmapMatrix?.height ?: 0
    val newBitmap = Bitmap.createBitmap(
        matrixWidth,
        matrixHeight,
        Bitmap.Config.ARGB_8888,
    )
    for (x in 0 until matrixWidth) {
        for (y in 0 until matrixHeight) {
            val pixelColor = if (bitmapMatrix?.get(x, y) == true) Color.BLACK else Color.WHITE
            newBitmap.setPixel(x, y, pixelColor)
        }
    }
    return newBitmap
}