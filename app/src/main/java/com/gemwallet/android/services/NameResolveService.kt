package com.gemwallet.android.services

import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.NameRecord


interface NameResolveService {
    suspend fun resolve(name: String, chain: Chain): NameRecord?
}

class GemNameResolveService(
    private val client: GemApiClient,
) : NameResolveService {
    override suspend fun resolve(name: String, chain: Chain): NameRecord? {
        return client.resolve(name, chain.string).getOrNull()
    }
}