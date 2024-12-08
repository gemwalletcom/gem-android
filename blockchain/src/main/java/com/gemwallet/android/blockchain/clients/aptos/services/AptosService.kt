package com.gemwallet.android.blockchain.clients.aptos.services

interface AptosService :
        AptosAccountsService,
        AptosBalancesService,
        AptosBroadcastService,
        AptosFeeService,
        AptosNodeStatusService,
        AptosTransactionsService