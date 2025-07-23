package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.clients.tron.services.TronCallService
import com.wallet.core.blockchain.tron.TronSmartContractResult

class FakeTronCallService(
    val responses: Map<String, TronSmartContractResult> = emptyMap()
) : TronCallService {
    val requests = mutableListOf<Map<String, String?>>()

    override suspend fun triggerSmartContract(request: Any): Result<TronSmartContractResult> {
        requests.add(request as Map<String, String?>)
        return Result.success(responses[request["contract_address"] ?: throw Exception()]
            ?: return Result.failure(Exception()))
    }
}