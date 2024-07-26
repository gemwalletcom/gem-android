package com.gemwallet.android.blockchain.clients

import com.wallet.core.primitives.AssetId
import java.math.BigInteger

interface SwapClient : BlockchainClient {

    suspend fun getAllowance(assetId: AssetId, owner: String, spender: String): BigInteger

    fun checkSpender(spender: String): Boolean

    fun isRequestApprove(): Boolean
}