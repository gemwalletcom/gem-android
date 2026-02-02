package com.gemwallet.android.data.coordinates

import com.gemwallet.android.application.GetAuthPayload
import com.gemwallet.android.application.PasswordStore
import com.gemwallet.android.application.device.coordinators.GetDeviceId
import com.gemwallet.android.blockchain.operators.LoadPrivateKeyOperator
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.data.services.gemapi.GemDeviceApiClient
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.math.toHexString
import com.wallet.core.primitives.AuthPayload
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Wallet
import uniffi.gemstone.GemAuthNonce
import wallet.core.jni.PrivateKey
import java.util.Arrays

class GetAuthPayloadImpl(
    private val gemDeviceApiClient: GemDeviceApiClient,
    private val getDeviceId: GetDeviceId,
    private val passwordStore: PasswordStore,
    private val loadPrivateKeyOperator: LoadPrivateKeyOperator,
) : GetAuthPayload {

    override suspend fun getAuthPayload(wallet: Wallet, chain: Chain): AuthPayload {
        val account = wallet.getAccount(chain) ?: throw Exception() // TODO
        val deviceId = getDeviceId.getDeviceId()
        val nonce = gemDeviceApiClient.getAuthNonce()
        val message = uniffi.gemstone.createAuthMessage(
            chain = Chain.Ethereum.string,
            address = account.address,
            authNonce = GemAuthNonce(nonce.nonce, nonce.timestamp)
        )
        val key = loadPrivateKeyOperator(
            wallet,
            chain,
            passwordStore.getPassword(wallet.id)
        )
        try {
            val signature = PrivateKey(key).sign(message.hash, WCChainTypeProxy()(chain).curve())
                .toHexString()
            return AuthPayload(
                deviceId = deviceId,
                chain = account.chain,
                address = account.address,
                nonce = nonce.nonce,
                signature = signature
            )
        } finally {
            Arrays.fill(key, 0)
        }
    }
}
