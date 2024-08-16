package com.gemwallet.android.blockchain.rpc

import retrofit2.Response

fun <T> Response<T>.getLatency() = raw().receivedResponseAtMillis - raw().sentRequestAtMillis