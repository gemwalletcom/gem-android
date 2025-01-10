package com.gemwallet.android.blockchain.rpc

sealed class ServiceError(message: String? = null, err: Throwable? = null) : Exception(message, err) {
    class ServerError(message: String? = null, err: Throwable? = null) : ServiceError(message, err)
    object EmptyHash : ServiceError()
    class BroadCastError(message: String? = null) : ServiceError(message)
}