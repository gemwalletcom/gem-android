package com.gemwallet.android.blockchain.clients.aptos.services

interface AptosServices :
        AptosAccountsService,
        AptosBalancesService,
        AptosBroadcastService,
        AptosFeeService,
        AptosNodeStatusService,
        AptosTransactionsService,
        AptosTokensService