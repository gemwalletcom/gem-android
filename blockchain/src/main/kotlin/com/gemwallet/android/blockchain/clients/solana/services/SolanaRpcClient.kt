package com.gemwallet.android.blockchain.clients.solana.services

interface SolanaRpcClient :
    SolanaAccountsService,
    SolanaBalancesService,
    SolanaStakeService,
    SolanaFeeService,
    SolanaNetworkInfoService,
    SolanaBroadcastService,
    SolanaTransactionsService,
    SolanaNodeStatusService {

    companion object {
        val commitmentKey: String = "commitment"
        val commitmentValue: String = "confirmed"
    }
}
