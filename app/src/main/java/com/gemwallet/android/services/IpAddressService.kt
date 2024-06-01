package com.gemwallet.android.services

import javax.inject.Inject
import javax.inject.Singleton

interface IpAddressService {
    suspend fun getIpAddress(): String?
}

@Singleton
class GemIpAddressService @Inject constructor(
    private val client: GemApiClient,
) : IpAddressService {
    override suspend fun getIpAddress(): String? =
        client.getIpAddress().body()?.ipv4

}