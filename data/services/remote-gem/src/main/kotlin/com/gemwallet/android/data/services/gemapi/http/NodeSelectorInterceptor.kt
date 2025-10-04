package com.gemwallet.android.data.services.gemapi.http

import android.util.Log
import com.gemwallet.android.cases.nodes.GetCurrentNodeCase
import com.gemwallet.android.cases.nodes.GetNodesCase
import com.gemwallet.android.cases.nodes.SetCurrentNodeCase
import com.gemwallet.android.ext.toChain
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class NodeSelectorInterceptor(
    private val getNodesCase: GetNodesCase,
    private val getCurrentNodeCase: GetCurrentNodeCase,
    private val setCurrentNodeCase: SetCurrentNodeCase,
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val blockchain = originalRequest.url.host.toChain() ?: return chain.proceed(originalRequest)
        val url = blockchain.getNodeUrl(getNodesCase, getCurrentNodeCase, setCurrentNodeCase)

        if (url != null) {
            val httpUrl = url.toHttpUrlOrNull()!!
            val request = setBaseUrl(originalRequest, httpUrl)
            try {
                return chain.proceed(request)
            } catch (err: Throwable) {
                Log.d("SelectNode", "Error", err)
            }
        }
        return chain.proceed(originalRequest)
    }

    private fun setBaseUrl(originalRequest: Request, baseUrl: HttpUrl): Request {
        val requestUrl = originalRequest.url.newBuilder()
            .scheme(baseUrl.scheme)
            .host(baseUrl.host)

        if (baseUrl.pathSegments.isNotEmpty() && !baseUrl.pathSegments.firstOrNull().isNullOrEmpty()) {
            val segments = baseUrl.pathSegments + originalRequest.url.pathSegments
            val originalSegmentsSize = originalRequest.url.pathSegments.size
            for (i in segments.indices) {
                if (i < originalSegmentsSize) {
                    requestUrl.setPathSegment(i, segments[i])
                } else {
                    requestUrl.addPathSegment(segments[i])
                }
            }
        }
        return originalRequest.newBuilder()
            .url(requestUrl.build())
            .build()
    }
}