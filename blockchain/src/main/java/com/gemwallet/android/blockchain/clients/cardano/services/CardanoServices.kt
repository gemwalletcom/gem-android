package com.gemwallet.android.blockchain.clients.cardano.services

interface CardanoServices : CardanoBalanceService,
    CardanoTransactionService,
    CardanoFeeService,
    CardanoBroadcastService,
    CardanoNodeStatusService