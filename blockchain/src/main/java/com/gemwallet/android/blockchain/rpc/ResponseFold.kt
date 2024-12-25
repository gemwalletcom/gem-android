package com.gemwallet.android.blockchain.rpc

import kotlinx.serialization.json.Json

inline fun <R, reified E, T>Result<T>.responseFold(onSuccess: (T) -> R, onError: (E) -> R, onFailure: (Throwable) -> R): R {
    val errorDeserializer = Json { ignoreUnknownKeys = true }

    return when (val exception = exceptionOrNull()) {
        null -> try {
            onSuccess((getOrNull() ?: throw Exception("Unknown error")) as T)
        } catch (err: Exception) {
            onFailure(err)
        }
        else -> {
            try {
                onError(errorDeserializer.decodeFromString(exception.message ?: "{}") ?: throw Exception())
            } catch (err: Throwable) {
                onFailure(exception)
            }
        }
    }
}