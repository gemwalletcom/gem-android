package com.gemwallet.android.ui.components.qr_code

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.TypedValue
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

class QRCodeGenerator {

    fun generate(
        context: Context,
        content: String,
        size: Int,
        logoAssetName: String? = null,
        padding: Int,
        logoQRScale: Float = 0.16f,
        cornerRadiusDp: Float = 16f
    ): Bitmap? {
        val qrCodeBitmap = createQRCode(content, size, padding) ?: return null
        return addBackgroundAndLogo(
            qrCodeBitmap = qrCodeBitmap,
            context = context,
            logoAssetName = logoAssetName,
            logoQRScale = logoQRScale,
            cornerRadiusDp = cornerRadiusDp,
            paddingPx = padding,
        )
    }

    private fun createQRCode(content: String, size: Int, padding: Int): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
                EncodeHintType.MARGIN to padding
            )

            val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
                for (x in 0 until size) {
                    for (y in 0 until size) {
                        setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun addBackgroundAndLogo(
        qrCodeBitmap: Bitmap,
        context: Context,
        logoAssetName: String?,
        logoQRScale: Float,
        cornerRadiusDp: Float,
        paddingPx: Int,
    ): Bitmap {
        val size = qrCodeBitmap.width
        val logoSize = (size * logoQRScale).toInt()
        val cornerRadiusPx = dpToPx(context, cornerRadiusDp)

        val outputBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)

        drawRoundedBackground(canvas, size, cornerRadiusPx, paddingPx)
        drawQRCodeOnCanvas(canvas, qrCodeBitmap, size)
        drawLogoBackground(canvas, qrCodeBitmap, logoSize, cornerRadiusPx)
        logoAssetName?.let {
            drawLogo(canvas, context, logoAssetName, logoSize, size)
        }

        return outputBitmap
    }

    private fun dpToPx(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )
    }

    private fun drawRoundedBackground(
        canvas: Canvas,
        size: Int,
        cornerRadiusPx: Float,
        paddingPx: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
        }
        val background = RectF(
            paddingPx.toFloat(),
            paddingPx.toFloat(),
            (size - paddingPx).toFloat(),
            (size - paddingPx).toFloat()
        )
        canvas.drawRoundRect(background, cornerRadiusPx, cornerRadiusPx, paint)
    }

    private fun drawQRCodeOnCanvas(canvas: Canvas, qrCodeBitmap: Bitmap, size: Int) {
        val qrCodeX = (size - qrCodeBitmap.width) / 2f
        val qrCodeY = (size - qrCodeBitmap.height) / 2f
        canvas.drawBitmap(qrCodeBitmap, qrCodeX, qrCodeY, null)
    }

    private fun drawLogoBackground(
        canvas: Canvas,
        qrCodeBitmap: Bitmap,
        logoSize: Int,
        cornerRadiusPx: Float
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = Color.WHITE
        val centerX = (qrCodeBitmap.width / 2).toFloat()
        val centerY = (qrCodeBitmap.height / 2).toFloat()
        val logoBackground = RectF(
            centerX - logoSize / 1.5f,
            centerY - logoSize / 1.5f,
            centerX + logoSize / 1.5f,
            centerY + logoSize / 1.5f
        )
        canvas.drawRoundRect(logoBackground, cornerRadiusPx, cornerRadiusPx, paint)

        paint.color = Color.BLACK
        val circularBackground = RectF(
            centerX - logoSize / 2f,
            centerY - logoSize / 2f,
            centerX + logoSize / 2f,
            centerY + logoSize / 2f
        )
        canvas.drawRoundRect(circularBackground, cornerRadiusPx, cornerRadiusPx, paint)
    }

    private fun drawLogo(
        canvas: Canvas,
        context: Context,
        logoAssetName: String,
        logoSize: Int,
        canvasSize: Int
    ) {
        val logoRaw = BitmapFactory.decodeStream(context.assets.open(logoAssetName))
        val scaledLogo = Bitmap.createScaledBitmap(logoRaw, logoSize, logoSize, false)
        val logoX = (canvasSize - scaledLogo.width) / 2f
        val logoY = (canvasSize - scaledLogo.height) / 2f
        canvas.drawBitmap(scaledLogo, logoX, logoY, null)
    }
}