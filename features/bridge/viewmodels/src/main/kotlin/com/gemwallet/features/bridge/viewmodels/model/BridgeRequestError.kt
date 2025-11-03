package com.gemwallet.features.bridge.viewmodels.model

sealed class BridgeRequestError(message: String) : Exception(message) {
    object ChainUnsupported : BridgeRequestError("chain not supported")
    object MethodUnsupported : BridgeRequestError("method not supported")
}