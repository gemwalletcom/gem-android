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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.AsyncImage
import com.gemwallet.android.localize.R
import com.gemwallet.android.ui.components.designsystem.Spacer8
import com.gemwallet.android.ui.components.qr_scanner.viewmodel.QRScannerViewModel
import com.gemwallet.android.ui.components.qr_scanner.viewmodel.ScannerState
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.actions.CancelAction
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
    permissionsGranted: Boolean,
    onCancel: CancelAction,
    onResult: (String) -> Unit,
) {
    val viewModel = QRScannerViewModel(permissionsGranted)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val scannerState by viewModel.scannerState.collectAsState()
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
        if (scannerState == ScannerState.Processing) {
            viewModel.reset()
        } else {
            onCancel()
        }
    }

    Scene(
        title = stringResource(id = R.string.wallet_scan_qr_code),
        onClose = { onCancel() },
        actions = {
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
                ScannerState.Idle -> {
                    CircularProgressIndicator()
                }

                ScannerState.Scanning -> {
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
                                            viewModel.scannerState.value = ScannerState.Success
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

                ScannerState.Processing -> {
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

                ScannerState.Success -> {
                    scanResult?.let { result ->
                        onResult(result)
                    }
                }

                is ScannerState.Error -> {
                    ErrorView(
                        errorType = scannerState,
                        onRetry = {
                            if (scannerState is ScannerState.Error.Unsupported) {
                                galleryLauncher.launch("image/*")
                            } else {
                                viewModel.setScanningState()
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorView(errorType: ScannerState, onRetry: () -> Unit) {
    val isUnsupportedError = errorType is ScannerState.Error.Unsupported

    val icon = if (isUnsupportedError) Icons.Filled.Warning else Icons.Filled.Error
    val title = stringResource(
        if (isUnsupportedError) R.string.errors_not_supported
        else R.string.errors_decoding
    )
    val retryButtonText = stringResource(
        if (isUnsupportedError) R.string.library_select_from_photo_library
        else R.string.common_try_again
    )

    val errorMessage = stringResource(
        if (isUnsupportedError) R.string.errors_not_supported_qr
        else R.string.errors_decoding_qr
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
        )

        Spacer8()

        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer8()

        Text(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 32.dp),
            text = errorMessage,
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer8()

        TextButton(
            onClick = onRetry,
        ) {
            Text(
                text = retryButtonText,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
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
