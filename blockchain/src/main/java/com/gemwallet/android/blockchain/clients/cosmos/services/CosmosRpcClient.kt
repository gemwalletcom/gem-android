package com.gemwallet.android.blockchain.clients.cosmos.services

interface CosmosRpcClient :
    CosmosBalancesService,
    CosmosAccountsService,
    CosmosStakeService,
    CosmosTransactionsService,
    CosmosBroadcastService,
    CosmosNodeStatusService