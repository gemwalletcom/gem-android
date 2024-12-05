package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.aptos.model.AptosAccount
import com.wallet.core.blockchain.aptos.models.AptosGasFee

internal val aptosAccountResponse = AptosAccount(
    sequence_number = "8",
    message = null,
    error_code = null,
)

internal val aptosFeeResponse = AptosGasFee(
    gas_estimate = 100,
    prioritized_gas_estimate = 150,
)