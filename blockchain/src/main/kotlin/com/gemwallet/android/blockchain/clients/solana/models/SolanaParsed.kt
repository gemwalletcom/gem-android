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
    val isInitialized: Boolean
)

class SolanaArrayData<T>(val data: List<T>)