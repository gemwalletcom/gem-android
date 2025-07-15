package com.gemwallet.android.blockchain.clients.solana.models

class SolanaParsedData<T>(
    val data: SolanaParsed<T>
)

class SolanaParsed<T>(
    val parsed: T
)

class SolanaInfo<T>(
    val info: T
)

class SolanaParsedSplTokenInfo(
    val decimals: Int,
    val extensions: List<Extension>? = null,
)

class SolanaArrayData<T>(val data: List<T>)

data class Extension (
    val extension: String,
    val state: ExtensionState
)

data class ExtensionState (
    val name: String?,
    val symbol: String?,
)