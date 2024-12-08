package com.gemwallet.android.blockchain.clients.bitcoin.services

import com.wallet.core.blockchain.bitcoin.models.BitcoinBlock
import com.wallet.core.blockchain.bitcoin.models.BitcoinNodeInfo
import retrofit2.Response

interface BitcoinRpcClient :
    BitcoinBalancesService,
    BitcoinUTXOService,
    BitcoinFeeService,
    BitcoinBroadcastService,
    BitcoinTransactionsService,
    BitcoinNodeStatusService

suspend fun BitcoinNodeStatusService.getBlock(url: String): Response<BitcoinBlock> {
    return block("$url/api/v2/block/1")
}

suspend fun BitcoinNodeStatusService.getNodeInfo(url: String): Result<BitcoinNodeInfo> {
    return nodeInfo("$url/api/v2")
}