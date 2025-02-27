package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.blockchain.clients.xrp.services.XrpAccountsService
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.wallet.core.blockchain.xrp.models.XRPAccount
import com.wallet.core.blockchain.xrp.models.XRPAccountResult
import com.wallet.core.blockchain.xrp.models.XRPResult

internal class TestXrpAccountsService(
    val balance: String = "",
) : XrpAccountsService {
    var requestAccount: String? = ""

    override suspend fun account(request: JSONRpcRequest<List<Map<String, String>>>): Result<XRPResult<XRPAccountResult>> {
        requestAccount = request.params[0]["account"]
        return Result.success(
            XRPResult(
                XRPAccountResult(
                    XRPAccount(
                        Balance = balance,
                        Sequence = 92788459,
                    ),
                    ledger_current_index = 1
                )
            )
        )
    }
}