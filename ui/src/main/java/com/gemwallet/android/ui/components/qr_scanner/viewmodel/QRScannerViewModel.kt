package com.gemwallet.android.ui.components.qr_scanner.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

class QRScannerViewModel(private val permissionsGranted: Boolean) {
    val scannerState = MutableStateFlow(
        if (!permissionsGranted) {
            ScannerState.Error.Unsupported
        } else {
            ScannerState.Scanning
        }
    )
    val scanResult = MutableStateFlow<String?>(null)
    val selectedImageUri = MutableStateFlow<Uri?>(null)

    suspend fun decodeQRCodeBitmap(bitmap: Bitmap) = withContext(Dispatchers.Default) {
        try {
            val result = decodeQRCodeFromBitmap(bitmap)
            scanResult.value = result
            scannerState.value = ScannerState.Success
        } catch (e: Exception) {
            scannerState.value = ScannerState.Error.Detection
        }
    }

    fun reset() {
        scannerState.value = ScannerState.Idle
        scanResult.value = null
        selectedImageUri.value = null
    }

    fun setScanningState() {
        scannerState.value = if (!permissionsGranted) {
            ScannerState.Error.Unsupported
        } else {
            ScannerState.Scanning
        }
    }

    fun setProcessingState(uri: Uri) {
        selectedImageUri.value = uri
        scannerState.value = ScannerState.Processing
    }

    private fun decodeQRCodeFromBitmap(bitmap: Bitmap): String? {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val luminanceSource = RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(luminanceSource))
        val reader = MultiFormatReader()
        return reader.decode(binaryBitmap).text
    }
}

sealed interface ScannerState {
    data object Idle : ScannerState
    data object Scanning : ScannerState
    data object Processing : ScannerState
    data object Success : ScannerState
    sealed interface Error : ScannerState {
        data object Unsupported : Error
        data object Detection : Error
    }
}