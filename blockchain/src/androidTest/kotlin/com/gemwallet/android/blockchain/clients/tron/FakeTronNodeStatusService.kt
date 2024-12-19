package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.clients.tron.services.TronNodeStatusService
import com.wallet.core.blockchain.tron.models.TronBlock
import com.wallet.core.blockchain.tron.models.TronChainParameter
import com.wallet.core.blockchain.tron.models.TronChainParameters
import com.wallet.core.blockchain.tron.models.TronHeader
import com.wallet.core.blockchain.tron.models.TronHeaderRawData
import retrofit2.Response

private val chainParamters = TronChainParameters(
    listOf(
        TronChainParameter("getCreateNewAccountFeeInSystemContract", 1000000),
        TronChainParameter("getCreateAccountFee", 100000),
        TronChainParameter("getEnergyFee", 210),
    )
)

class FakeTronNodeStatusService(
    private val chainParameters: TronChainParameters? = chainParamters,
    private val tronBlock: TronBlock? = TronBlock(
        TronHeaderRawData(
            TronHeader(
                number = 67978303,
                version = 31,
                txTrieRoot = "e02c6d0bab5f0c6d1f61051646535ed76f6e1ef0db5c32069cf612123ce976b3",
                witness_address = "41c05142fd1ca1e03688a43585096866ae658f2cb2",
                parentHash = "00000000040d443e37c9fcdfb11ba6fb9ed08682f0f492b3b4ab6639b9198694",
                timestamp = 1734585603000,
            )
        )
    ),
) : TronNodeStatusService {

    override suspend fun getChainParameters(): Result<TronChainParameters> {
        return Result.success(chainParameters ?: return Result.failure(Exception()))
    }

    override suspend fun nowBlock(): Result<TronBlock> {
        return Result.success(tronBlock ?: return Result.failure(Exception()))
    }

    override suspend fun nowBlock(url: String): Response<TronBlock> {
        TODO("Not yet implemented")
    }


}