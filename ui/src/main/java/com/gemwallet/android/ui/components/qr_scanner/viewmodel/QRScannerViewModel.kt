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

class QRScannerViewModel {
    val scannerState = MutableStateFlow(ScannerState.SCANNING)
    val errorMessage = MutableStateFlow<String?>(null)
    val scanResult = MutableStateFlow<String?>(null)
    val selectedImageUri = MutableStateFlow<Uri?>(null)

    suspend fun decodeQRCodeBitmap(bitmap: Bitmap) = withContext(Dispatchers.Default) {
        try {
            val result = decodeQRCodeFromBitmap(bitmap)
            scanResult.value = result
            scannerState.value = ScannerState.SUCCESS
        } catch (e: Exception) {
            errorMessage.value = "Failed to decode QR code"
            scannerState.value = ScannerState.ERROR
        }
    }

    fun reset() {
        scannerState.value = ScannerState.IDLE
        errorMessage.value = null
        scanResult.value = null
        selectedImageUri.value = null
    }

    fun setScanningState() {
        scannerState.value = ScannerState.SCANNING
    }

    fun setProcessingState(uri: Uri) {
        selectedImageUri.value = uri
        scannerState.value = ScannerState.PROCESSING
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

enum class ScannerState {
    IDLE, SCANNING, PROCESSING, SUCCESS, ERROR
}