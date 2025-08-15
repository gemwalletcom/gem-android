package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.aptos.services.AptosBalancesService
import com.wallet.core.blockchain.aptos.AptosResource
import com.wallet.core.blockchain.aptos.AptosResourceBalanceOptional
import com.wallet.core.blockchain.aptos.AptosResourceCoin
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody

class TestAptosBalancesService(
    val nativeBalance: String? = null,
) : AptosBalancesService {

    var nativeRequest: String? = null
    var tokenRequest: String? = null

    override suspend fun balance(address: String, assetType: String): ResponseBody {
        nativeRequest = address
        nativeBalance ?: throw Exception()
        return nativeBalance.toResponseBody()
    }
    override suspend fun resources(address: String): List<AptosResource<AptosResourceBalanceOptional>> {
        tokenRequest = address
        return listOf(
            AptosResource(
                type = "0x1::coin::CoinStore<0x1::aptos_coin::AptosCoin>",
                AptosResourceBalanceOptional(
                    AptosResourceCoin(
                        "10000000"
                    )
                )
            ),
            AptosResource(
                type = "0x1::coin::CoinStore<0xe4ccb6d39136469f376242c31b34d10515c8eaaa38092f804db8e08a8f53c5b2::assets_v1::EchoCoin002>",
                AptosResourceBalanceOptional(
                    AptosResourceCoin(
                        "30000000"
                    )
                )
            ),
            AptosResource(
                type = "0x1::coin::CoinStore<0x159df6b7689437016108a019fd5bef736bac692b6d4a1f10c941f6fbb9a74ca6::oft::CakeOFT>",
                AptosResourceBalanceOptional(
                    AptosResourceCoin(
                        "50000000"
                    )
                )
            ),
        )
    }
}