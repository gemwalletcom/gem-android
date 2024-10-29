package com.gemwallet.android.interactors.sync

import com.gemwallet.android.data.config.ConfigRepository
import com.gemwallet.android.data.wallet.WalletsRepository
import com.gemwallet.android.interactors.SyncOperator
import com.gemwallet.android.services.GemApiClient
import com.wallet.core.primitives.Subscription

class SyncSubscription(
    private val gemApiClient: GemApiClient,
    private val walletsRepository: WalletsRepository,
    private val configRepository: ConfigRepository,
) : SyncOperator {

    override suspend fun invoke() {
        val deviceId = configRepository.getDeviceId()
        val wallets = walletsRepository.getAll()
        val subscriptionsIndex = mutableMapOf<String, Subscription>()

        wallets.forEach { wallet ->
            wallet.accounts.forEach { account ->
                subscriptionsIndex["${account.chain.string}_${account.address}_${wallet.index}"] = Subscription(
                    chain = account.chain,
                    address = account.address,
                    wallet_index = wallet.index,
                )
            }
        }

        val result = gemApiClient.getSubscriptions(deviceId)
        val remoteSubscriptions = result.getOrNull() ?: emptyList()
        remoteSubscriptions.forEach {
            subscriptionsIndex.remove("${it.chain.string}_${it.address}_${it.wallet_index}")
        }
        if (subscriptionsIndex.isNotEmpty()) {
            gemApiClient.addSubscriptions(deviceId, subscriptionsIndex.values.toList())
            configRepository.increaseSubscriptionVersion()
        }
    }
}