package com.gemwallet.android.blockchain.clients.tron.services

import com.gemwallet.android.ext.asset
import com.wallet.core.blockchain.tron.models.TronAccount
import com.wallet.core.primitives.Chain
import java.math.BigInteger

fun TronAccount.staked(chain: Chain): BigInteger {
    val votes = votes ?: emptyList()
    val totalVotes = votes.fold(0L) { acc, item -> acc + item.vote_count }
    return BigInteger.valueOf(totalVotes) * BigInteger.TEN.pow(chain.asset().decimals)
}