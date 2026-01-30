package com.gemwallet.android.data.services.gemapi.http

import com.gemwallet.android.application.PasswordStore
import com.gemwallet.android.math.decodeHex
import okhttp3.Interceptor
import okhttp3.Response
import wallet.core.jni.Base64
import wallet.core.jni.Curve
import wallet.core.jni.HDWallet
import wallet.core.jni.PrivateKey

class SecurityInterceptor(
    private val passwordStore: PasswordStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val privateKey = getPrivateKey()
        val request = chain.request()
        val method = request.method
        val path = request.url.encodedPath
        val bodyHash = request.body?.sha256()?.hex() ?: ""
        val time = System.currentTimeMillis()
        val message = "v1.${time}.${method}.${path}.${bodyHash}"
        val signature = Base64.encode(privateKey.sign(message.toByteArray(), Curve.CURVE25519))
        return chain.proceed(
            request.newBuilder()
                .header("x-device-signature", signature)
                .header("x-device-timestamp", time.toHexString())
                .header("x-device-body-hash", bodyHash)
                .build()
        )
    }

    private fun getPrivateKey(): PrivateKey {
        try {
            val data = passwordStore.getPassword("gem_api_pk")
            return PrivateKey(data.decodeHex())
        } catch (_: Throwable) {}
        val deviceKey = HDWallet(128, "").getMasterKey(Curve.CURVE25519)
        passwordStore.putPassword("gem_api_pk", deviceKey.data().toHexString())
        return deviceKey
    }
}