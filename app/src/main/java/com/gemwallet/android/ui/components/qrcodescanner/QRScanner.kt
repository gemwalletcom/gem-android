package com.gemwallet.android.ui.components.qrcodescanner

import android.Manifest
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.ImageFormat
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.gemwallet.android.R
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.theme.padding16
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import kotlin.math.min

@kotlin.OptIn(ExperimentalPermissionsApi::class)
@ExperimentalGetImage
@Composable
fun qrCodeRequest(
    onCancel: () -> Unit,
    onResult: (String) -> Unit,
): Boolean {
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    BackHandler(true) {
        onCancel()
    }
    return if (cameraPermissionState.status.isGranted) {
        QRScannerScene(onCancel, onResult)
        true
    } else {
        AlertDialog(
            onDismissRequest = onCancel,
            text = {
                Text(text = stringResource(id = R.string.camera_permission_request_camera))
            },
            confirmButton = {
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text(text = stringResource(id = R.string.common_grant_permission))
                }
            },
            dismissButton = {
                Button(onClick = onCancel) {
                    Text(text = stringResource(id = R.string.common_cancel))
                }
            }
        )
        false
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun QRScannerScene(
    onCancel: () -> Unit,
    onResult: (String) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageResult by remember { mutableStateOf("") }
    var imageError by remember { mutableStateOf("") }
    val galleryLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
        imageResult = ""
        imageError = ""
    }
    val cancel = {
        imageUri = null
        imageError = ""
        imageResult = ""
    }
    LaunchedEffect(imageUri) {
        val image = imageUri ?: return@LaunchedEffect
        coroutineScope.launch(Dispatchers.IO) {
            val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, image))
                .copy(Bitmap.Config.RGBA_F16, true)
            val intArray = IntArray(bitmap.getWidth() * bitmap.getHeight())
            bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight())

            try {
                val source = RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray)
                val binaryBmp = BinaryBitmap(HybridBinarizer(source))
                val result = MultiFormatReader().apply {
                    setHints(
                        mapOf(DecodeHintType.POSSIBLE_FORMATS to arrayListOf(BarcodeFormat.QR_CODE))
                    )
                }.decode(binaryBmp)
                imageResult = uniffi.gemstone.paymentDecodeUrl(result.text).address
                if (imageResult.isEmpty()) {
                    throw Exception()
                }
            } catch (e: Exception) {
                imageError = e.message ?: "Unknown error"
            }
        }
    }
    BackHandler(imageUri != null) {
        cancel()
    }
    Scene(
        title = stringResource(id = R.string.wallet_scan_qr_code),
        actions = {
            IconButton(
                modifier = Modifier.padding(padding16),
                onClick = { galleryLauncher.launch("image/*") }
            ) {
                Icon(imageVector = Icons.Default.Image, contentDescription = "from_image")
            }
            if (imageUri != null) {
                IconButton(onClick = cancel) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "close_image")
                }
            }
        },
        onClose = { if (imageUri == null) onCancel() else cancel() },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            QRScanner(listener = onResult)
            if (imageUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    coil.compose.AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUri)
                            .diskCachePolicy(policy = CachePolicy.ENABLED)
                            .networkCachePolicy(policy = CachePolicy.ENABLED)
                            .build(),
                        contentDescription = "",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (imageResult.isNotEmpty()) {
                        Column(
                            modifier = Modifier.padding(40.dp).align(Alignment.BottomCenter),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(padding16)
                                    .background(Color.Black, MaterialTheme.shapes.medium)
                                    .padding(padding16),
                                text = imageResult,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W300)
                            )

                            Button(
                                onClick = { onResult(imageResult) }
                            ) {
                                Text(text = stringResource(id = R.string.common_done))
                            }
                        }
                    }
                    if (imageError.isNotEmpty()) {
                        Text(
                            modifier = Modifier
                                .padding(40.dp).align(Alignment.BottomCenter)
                                .padding(padding16)
                                .background(Color.Black, MaterialTheme.shapes.medium)
                                .padding(padding16),
                            text = "Image doesn't contains qr code or data",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W300)
                        )
                    }
                }
            }
        }
    }
}

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
                    it.surfaceProvider = previewView.surfaceProvider
                }
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { imageAnalysis ->
                    imageAnalysis.setAnalyzer(
                        ContextCompat.getMainExecutor(context),
                        QRCodeAnalyzer(callback = {
                            imageAnalysis.clearAnalyzer()
                            listener.invoke(it)
                        })
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
            FinderView()
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
    }

}

@Composable
private fun FinderView() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val halfFullWidth = size.width / 2f
        val halfFullHeight = size.height / 2f
        val rectSize = min(size.width, size.height) * 0.7f
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

private fun ByteBuffer.toByteArray(): ByteArray {
    rewind()
    return ByteArray(remaining()).also {
        get(it)
    }
}