package com.gemwallet.android.ui.components.qrcodescanner

import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer
import kotlin.math.min


@ExperimentalGetImage
@Composable
fun QRScanner(listener: (String) -> Unit) {
    val localContext = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFeature = remember {
        ProcessCameraProvider.getInstance(localContext)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView({ context ->
            val previewView = PreviewView(context).also {
                it.scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(
                        ContextCompat.getMainExecutor(context),
                        QRCodeAnalyzer(callback = listener)
                    )
                }
            val selector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()
            try {
                val provider = cameraProviderFeature.get()
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    selector,
                    preview,
                    imageAnalyzer,
                )
            } catch (err: Throwable) {
                Log.d("QR_CODE_SCANNER", "Error", err)
            }
            previewView
        }, modifier = Modifier.fillMaxSize())
        Box(modifier = Modifier
            .fillMaxSize()
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val halfFullWidth = size.width / 2f
                val halfFullHeight = size.height / 2f
                val rectSize = min(size.width, size.height) * 0.5f
                val rectHalfSize = rectSize / 2f
                val viewFrameSize = (rectHalfSize / 2f)

                clipRect(
                    left = halfFullWidth - rectHalfSize + 10,
                    top = halfFullHeight - rectHalfSize + 10,
                    right = halfFullWidth + rectHalfSize - 10,
                    bottom = halfFullHeight + rectHalfSize - 10,
                    clipOp = ClipOp.Difference
                ) {
                    drawRect(Color.Black.copy(alpha = 0.7f), topLeft = Offset.Zero, size)
                }
                val path = Path()
                path.moveTo(halfFullWidth - rectHalfSize, halfFullHeight - viewFrameSize)
                path.lineTo(halfFullWidth - rectHalfSize, halfFullHeight - rectHalfSize)
                path.lineTo(halfFullWidth - viewFrameSize, halfFullHeight - rectHalfSize)

                path.moveTo(halfFullWidth + viewFrameSize, halfFullHeight - rectHalfSize)
                path.lineTo(halfFullWidth + rectHalfSize, halfFullHeight - rectHalfSize)
                path.lineTo(halfFullWidth + rectHalfSize, halfFullHeight - viewFrameSize)

                path.moveTo(halfFullWidth + rectHalfSize, halfFullHeight + viewFrameSize)
                path.lineTo(halfFullWidth + rectHalfSize, halfFullHeight + rectHalfSize)
                path.lineTo(halfFullWidth + viewFrameSize, halfFullHeight + rectHalfSize)

                path.moveTo(halfFullWidth - rectHalfSize, halfFullHeight + viewFrameSize)
                path.lineTo(halfFullWidth - rectHalfSize, halfFullHeight + rectHalfSize)
                path.lineTo(halfFullWidth - viewFrameSize, halfFullHeight + rectHalfSize)

                drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(width = 20.0f, join = StrokeJoin.Round)
                )
            }
        }
    }
}

@ExperimentalGetImage
private class QRCodeAnalyzer(
    val callback: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val supportedImageFormats = listOf(
        ImageFormat.YUV_420_888,
        ImageFormat.YUV_422_888,
        ImageFormat.YUV_444_888
    )

    override fun analyze(imageProxy: ImageProxy) {
        if (imageProxy.format !in supportedImageFormats) {
            return
        }
        val bytes = imageProxy.planes.first().buffer.toByteArray()
        val source = PlanarYUVLuminanceSource(
            bytes,
            imageProxy.width,
            imageProxy.height,
            0,
            0,
            imageProxy.width,
            imageProxy.height,
            false
        )
        val binaryBmp = BinaryBitmap(HybridBinarizer(source))
        try {
            val result = MultiFormatReader().apply {
                setHints(
                    mapOf(DecodeHintType.POSSIBLE_FORMATS to arrayListOf(BarcodeFormat.QR_CODE))
                )
            }.decode(binaryBmp)
            callback(result.text)
        } catch (e: Exception) {
//            Quite
        } finally {
            imageProxy.close()
        }
        imageProxy.close()
    }
}

private fun ByteBuffer.toByteArray(): ByteArray {
    rewind()
    return ByteArray(remaining()).also {
        get(it)
    }
}