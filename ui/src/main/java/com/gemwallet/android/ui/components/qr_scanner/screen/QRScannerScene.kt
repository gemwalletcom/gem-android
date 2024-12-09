package com.gemwallet.android.ui.components.qr_scanner.screen

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.ImageFormat
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis.Builder
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.AsyncImage
import com.gemwallet.android.localize.R
import com.gemwallet.android.ui.components.qr_scanner.viewmodel.QRScannerViewModel
import com.gemwallet.android.ui.components.qr_scanner.viewmodel.ScannerState
import com.gemwallet.android.ui.components.screen.Scene
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

@OptIn(ExperimentalGetImage::class)
@Composable
fun QRScannerScene(
    onCancel: () -> Unit,
    onResult: (String) -> Unit,
) {
    val viewModel = QRScannerViewModel()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val scannerState by viewModel.scannerState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val scanResult by viewModel.scanResult.collectAsState()
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                viewModel.setProcessingState(uri)
            }
        }

    BackHandler {
        if (scannerState == ScannerState.PROCESSING) {
            viewModel.reset()
        } else {
            onCancel()
        }
    }

    Scene(
        title = stringResource(id = R.string.wallet_scan_qr_code),
        onClose = onCancel,
        mainAction = {
            IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                Icon(imageVector = Icons.Default.Image, contentDescription = "Select from gallery")
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (scannerState) {
                ScannerState.IDLE -> {
                    CircularProgressIndicator()
                }

                ScannerState.SCANNING -> {
                    AndroidView(
                        factory = { context ->
                            val previewView = PreviewView(context).apply {
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }

                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().apply {
                                surfaceProvider = previewView.surfaceProvider
                            }

                            val imageAnalysis = Builder()
                                .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .apply {
                                    setAnalyzer(
                                        ContextCompat.getMainExecutor(context),
                                        QRCodeAnalyzer { result ->
                                            viewModel.scanResult.value = result
                                            viewModel.scannerState.value = ScannerState.SUCCESS
                                        }
                                    )
                                }

                            val cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                                .build()

                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )

                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                ScannerState.PROCESSING -> {
                    selectedImageUri?.let { uri ->
                        LaunchedEffect(uri) {
                            viewModel.decodeQRCodeBitmap(
                                ImageDecoder.decodeBitmap(
                                    ImageDecoder.createSource(
                                        context.contentResolver,
                                        uri
                                    )
                                ).copy(Bitmap.Config.RGBA_F16, true)
                            )
                        }
                        AsyncImage(
                            model = uri,
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                ScannerState.SUCCESS -> {
                    scanResult?.let { result ->
                        SuccessView(result = result) { onResult(it) }
                    }
                }

                ScannerState.ERROR -> {
                    ErrorView(
                        errorMessage = errorMessage ?: "Unknown error",
                        onRetry = { viewModel.setScanningState() },
                        onCancel = onCancel
                    )
                }
            }
        }
    }
}


@Composable
fun ErrorView(errorMessage: String, onRetry: () -> Unit, onCancel: () -> Unit) {

}

@Composable
fun SuccessView(result: String, onResult: (String) -> Unit) {

}

@ExperimentalGetImage
private class QRCodeAnalyzer(
    val callback: (String) -> Unit
) : androidx.camera.core.ImageAnalysis.Analyzer {
    private val supportedImageFormats = listOf(
        ImageFormat.YUV_420_888,
        ImageFormat.YUV_422_888,
        ImageFormat.YUV_444_888
    )

    override fun analyze(imageProxy: androidx.camera.core.ImageProxy) {
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
        } catch (_: Exception) {
//            Quite
        } finally {
            imageProxy.close()
        }
    }
}

private fun ByteBuffer.toByteArray(): ByteArray {
    rewind()
    return ByteArray(remaining()).also {
        get(it)
    }
}
