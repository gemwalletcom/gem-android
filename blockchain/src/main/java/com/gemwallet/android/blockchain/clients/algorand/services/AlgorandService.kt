package com.gemwallet.android.blockchain.clients.algorand.services

interface AlgorandService : AlgorandAccountService,
        AlgorandBroadcastService,
        AlgorandTxStatusService,
        AlgorandNodeStatusService