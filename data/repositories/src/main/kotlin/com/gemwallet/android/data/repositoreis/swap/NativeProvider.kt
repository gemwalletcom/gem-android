package com.gemwallet.android.data.repositoreis.swap

import com.gemwallet.android.cases.nodes.GetCurrentNodeCase
import com.gemwallet.android.cases.nodes.GetNodesCase
import com.gemwallet.android.cases.nodes.SetCurrentNodeCase
import com.gemwallet.android.data.services.gemapi.http.getNodeUrl
import com.gemwallet.android.ext.toChain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import uniffi.gemstone.AlienProvider
import uniffi.gemstone.AlienTarget

class NativeProvider(
    private val getNodesCase: GetNodesCase,
    private val getCurrentNodeCase: GetCurrentNodeCase,
    private val setCurrentNodeCase: SetCurrentNodeCase,
    private val httpClient: OkHttpClient = OkHttpClient(),
): AlienProvider {

    override fun getEndpoint(chain: uniffi.gemstone.Chain): String {
        return chain.toChain()?.getNodeUrl(getNodesCase, getCurrentNodeCase, setCurrentNodeCase)
            ?: throw IllegalArgumentException("Can't found node url for chain: $chain")
    }

    override suspend fun batchRequest(targets: List<AlienTarget>): List<ByteArray> = withContext(Dispatchers.IO) {
        targets.map { target -> async { request(target) } }.awaitAll()
    }

    override suspend fun request(target: AlienTarget): ByteArray = withContext(Dispatchers.IO) {
        async {
            val requestBuilder = Request.Builder()
                .url(target.url)
                .method(target.method.name, target.body?.toRequestBody())
            target.headers?.forEach {
                requestBuilder.addHeader(it.key, it.value)
            }
            val response = httpClient.newCall(requestBuilder.build()).execute()
            val data = response.body?.bytes() ?: byteArrayOf()
            if (response.code != 200 && data.isEmpty()) {
                throw Exception("Invalid HTTP status code: ${response.code})")
            }
            data
        }.await()
    }
}