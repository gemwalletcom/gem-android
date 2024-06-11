package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.SignerParams
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SignPreloaderProxy(
    private val preloaders: List<SignerPreload>
) : SignerPreload {
    override suspend fun invoke(
        owner: Account,
        params: ConfirmParams,
    ): Result<SignerParams> = withContext(Dispatchers.IO) {
        try {
            preloaders.firstOrNull { it.isMaintain(owner.chain) }
                ?.invoke(owner = owner, params = params) ?: Result.failure(
                IllegalArgumentException(
                    "Chain isn't support"
                )
            )
        } catch (err: Throwable) {
            Result.failure(err)
        }
    }

    override fun maintainChain(): Chain {
        throw Exception()
    }
}