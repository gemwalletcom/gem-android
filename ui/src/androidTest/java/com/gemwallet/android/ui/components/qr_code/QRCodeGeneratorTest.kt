package com.gemwallet.android.ui.components.qr_code

import android.content.Context
import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.zxing.BinaryBitmap
import com.google.zxing.LuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QRCodeGeneratorMockKTest {

    private lateinit var qrCodeGenerator: QRCodeGenerator

    @Before
    fun setUp() {
        qrCodeGenerator = QRCodeGenerator()
    }

    @Test
    fun generateQRCodeAndVerifyScannedContentMatchesInput() {
        val content = "test_content"
        val size = 512
        val padding = 10

        val mockContext = mockk<Context>(relaxed = true)

        val qrCodeBitmap = qrCodeGenerator.generate(
            context = mockContext,
            content = content,
            size = size,
            padding = padding
        )

        assertNotNull("QR Code bitmap should not be null", qrCodeBitmap)
        assert(qrCodeBitmap!!.width > 0 && qrCodeBitmap.height > 0) {
            "QR Code bitmap dimensions must be greater than 0"
        }

        val decodedContent = decodeQRCode(qrCodeBitmap)
        assertNotNull("Decoded QR Code content should not be null", decodedContent)
        assertEquals(content, decodedContent)
    }

    private fun decodeQRCode(bitmap: Bitmap): String? {
        return try {
            val intArray = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

            val source: LuminanceSource = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            val reader = QRCodeReader()
            reader.decode(binaryBitmap).text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}