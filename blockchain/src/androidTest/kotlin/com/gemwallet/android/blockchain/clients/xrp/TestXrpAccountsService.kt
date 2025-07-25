package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.blockchain.clients.xrp.services.XrpAccountsService
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.wallet.core.blockchain.xrp.XRPAccount
import com.wallet.core.blockchain.xrp.XRPAccountLinesResult
import com.wallet.core.blockchain.xrp.XRPAccountResult
import com.wallet.core.blockchain.xrp.XRPResult

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
                        OwnerCount = 1,
                    ),
                    ledger_current_index = 1
                )
            )
        )
    }

    override suspend fun accountLines(request: JSONRpcRequest<List<Map<String, String>>>): Result<XRPResult<XRPAccountLinesResult>> {
        return Result.success(
            XRPResult(
                XRPAccountLinesResult(
                    listOf()
                )
            )
        )
    }
}