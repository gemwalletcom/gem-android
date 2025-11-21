package com.gemwallet.features.bridge.viewmodels.model

sealed class BridgeRequestError(message: String) : Exception(message) {
    object ScamSession : BridgeRequestError("Scam session")
    object ChainUnsupported : BridgeRequestError("chain not supported")
    object MethodUnsupported : BridgeRequestError("method not supported")
    object UnresolvedChainId : BridgeRequestError("Unresolved chain id")
}