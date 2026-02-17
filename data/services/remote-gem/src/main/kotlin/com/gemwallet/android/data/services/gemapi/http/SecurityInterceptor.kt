package com.gemwallet.android.data.services.gemapi.http

import com.gemwallet.android.application.device.coordinators.GetDeviceId
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.math.toHexString
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import wallet.core.jni.Base64
import wallet.core.jni.Curve
import wallet.core.jni.Hash
import wallet.core.jni.PrivateKey

class SecurityInterceptor(
    private val getDeviceId: GetDeviceId,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val method = request.method
        val path = request.url.encodedPath

        val body = request.body?.let {
            val buffer: Buffer = Buffer()
            it.writeTo(buffer)
            val data = buffer.readUtf8()
            Hash.sha256(data.toByteArray())
        }?.toHexString("") ?: ""
        val time = System.currentTimeMillis()
        val message = "v1.${time}.${method}.${path}.${body}"
        val signature = Base64.encode(
            PrivateKey(getDeviceId.getDeviceKey().decodeHex())
                .sign(message.toByteArray(), Curve.ED25519)
        )
        val deviceId = getDeviceId.getDeviceId()
        return try {
            chain.proceed(
                request.newBuilder()
                    .header("x-device-signature", signature)
                    .header("x-device-timestamp", time.toString())
                    .header("x-device-body-hash", body)
                    .header("x-device-id", deviceId)
                    .build()
            )
        } catch (err: Throwable) {
            Response.Builder()
                .code(503)
                .message("HTTP Exception: ${err.message}")
                .request(chain.request())
                .build()
        }
    }
}