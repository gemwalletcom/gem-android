package com.gemwallet.android.blockchain.clients.stellar.services

interface StellarService : StellarAccountService,
        StellarBroadcastService,
        StellarFeeService,
        StellarTxStatusService,
        StellarNodeStatusService