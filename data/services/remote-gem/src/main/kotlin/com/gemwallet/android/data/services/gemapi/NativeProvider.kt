package com.gemwallet.android.data.services.gemapi

import com.gemwallet.android.cases.nodes.GetCurrentNodeCase
import com.gemwallet.android.cases.nodes.GetNodesCase
import com.gemwallet.android.cases.nodes.SetCurrentNodeCase
import com.gemwallet.android.data.services.gemapi.http.getNodeUrl
import com.gemwallet.android.ext.toChain
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import uniffi.gemstone.AlienProvider
import uniffi.gemstone.AlienResponse
import uniffi.gemstone.AlienTarget
import uniffi.gemstone.Chain

class NativeProvider(
    private val getNodesCase: GetNodesCase,
    private val getCurrentNodeCase: GetCurrentNodeCase,
    private val setCurrentNodeCase: SetCurrentNodeCase,
    private val httpClient: OkHttpClient = OkHttpClient(),
): AlienProvider {

    override fun getEndpoint(chain: Chain): String {
        return chain.toChain()?.getNodeUrl(getNodesCase, getCurrentNodeCase, setCurrentNodeCase)
            ?: throw IllegalArgumentException("Can't found node url for chain: $chain")
    }

    override suspend fun request(target: AlienTarget): AlienResponse {
        val requestBuilder = Request.Builder()
            .url(target.url)
            .method(target.method.name, target.body?.toRequestBody())
        target.headers?.forEach {
            requestBuilder.addHeader(it.key, it.value)
        }
        return try {
            val response = httpClient.newCall(requestBuilder.build()).execute()
            val data = response.body?.bytes() ?: byteArrayOf()
            val status = response.code.toUShort()
            AlienResponse(status, data)
        } catch (_: Throwable) {
            AlienResponse(500.toUShort(), byteArrayOf())
        }
    }
}