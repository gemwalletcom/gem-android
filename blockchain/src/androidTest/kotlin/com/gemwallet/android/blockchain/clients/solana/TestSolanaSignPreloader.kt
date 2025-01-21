package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.solana.models.SolanaTokenOwner
import com.gemwallet.android.blockchain.clients.solana.services.SolanaNetworkInfoService
import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.DestinationAddress
import com.wallet.core.blockchain.solana.models.SolanaBlockhash
import com.wallet.core.blockchain.solana.models.SolanaBlockhashResult
import com.wallet.core.blockchain.solana.models.SolanaValue
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.SolanaTokenProgramId
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.math.BigInteger

class TestSolanaSignPreloader {

    companion object {
        init {
            includeLibs()
        }
    }

    private class TestSolanaNetworkInfoService(
        val tokenOwner: String = "",
        val blockhash: String = "",
    ) : SolanaNetworkInfoService {
        var tokenIdRequest: String = ""

        override suspend fun getTokenInfo(request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SolanaValue<SolanaTokenOwner>>> {
            tokenIdRequest = request.params[0].toString()

            return Result.success(
                JSONRpcResponse(SolanaValue(SolanaTokenOwner(tokenOwner)))
            )
        }

        override suspend fun getBlockhash(request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<SolanaBlockhashResult>> {
            return Result.success(JSONRpcResponse(SolanaBlockhashResult(SolanaBlockhash(blockhash))))
        }

    }

    @Test
    fun testSolana_netive_transfer_preload() {
        val preloader = SolanaSignerPreloader(
            chain = Chain.Solana,
            feeService = TestSolanaFeeService(),
            networkInfoService = TestSolanaNetworkInfoService(blockhash = "123"),
            accountsService = TestSolanaAccountsService(),
        )

        val result = runBlocking {
            preloader.preloadNativeTransfer(
                ConfirmParams.Builder(
                    assetId = AssetId(Chain.Solana),
                    from = Account(Chain.Solana, "AGkXQZ9qm99xukisDUHvspWHESrcjs8Y4AmQQgef3BRh", ""),
                    amount = BigInteger.valueOf(10_000_000)
                )
                .transfer(destination = DestinationAddress("AGkXQZ9qm99xukisDUHvspWHESrcjs8Y4AmQQgef3BRh")) as ConfirmParams.TransferParams.Native
            )
        }
        assertEquals(BigInteger("300004992"), result.chainData.gasFee().amount)
        assertEquals("123", (result.chainData as SolanaSignerPreloader.SolanaChainData).blockhash)
        assertEquals(SolanaTokenProgramId.Token, (result.chainData as SolanaSignerPreloader.SolanaChainData).tokenProgram)
        assertEquals(null, (result.chainData as SolanaSignerPreloader.SolanaChainData).recipientTokenAddress)
        assertEquals("", (result.chainData as SolanaSignerPreloader.SolanaChainData).senderTokenAddress)
    }

    @Test
    fun testSolana_transfer_token_preload() {
        val preloader = SolanaSignerPreloader(
            chain = Chain.Solana,
            feeService = TestSolanaFeeService(),
            networkInfoService = TestSolanaNetworkInfoService(blockhash = "123", tokenOwner = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"),
            accountsService = TestSolanaAccountsService("DVWPV7brSbPDkA7a3qdn6UJsVc3J3DyhQhjNaZeZqwzo"),
        )

        val result = runBlocking {
            preloader.preloadTokenTransfer(
                ConfirmParams.Builder(
                    assetId = AssetId(Chain.Solana, "Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB"),
                    from = Account(Chain.Solana, "AGkXQZ9qm99xukisDUHvspWHESrcjs8Y4AmQQgef3BRh", ""),
                    amount = BigInteger.valueOf(10_000_000)
                )
                .transfer(destination = DestinationAddress("AGkXQZ9qm99xukisDUHvspWHESrcjs8Y4AmQQgef3BRh")) as ConfirmParams.TransferParams.Token
            )
        }
        assertEquals(BigInteger("300004992"), result.chainData.gasFee().amount)
        assertEquals("123", (result.chainData as SolanaSignerPreloader.SolanaChainData).blockhash)
        assertEquals(SolanaTokenProgramId.Token, (result.chainData as SolanaSignerPreloader.SolanaChainData).tokenProgram)
        assertEquals("DVWPV7brSbPDkA7a3qdn6UJsVc3J3DyhQhjNaZeZqwzo", (result.chainData as SolanaSignerPreloader.SolanaChainData).recipientTokenAddress)
        assertEquals("DVWPV7brSbPDkA7a3qdn6UJsVc3J3DyhQhjNaZeZqwzo", (result.chainData as SolanaSignerPreloader.SolanaChainData).senderTokenAddress)
    }

    @Test
    fun testSolana_transfer_token2022_preload() {
        val preloader = SolanaSignerPreloader(
            chain = Chain.Solana,
            feeService = TestSolanaFeeService(),
            networkInfoService = TestSolanaNetworkInfoService(blockhash = "123", tokenOwner = "TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb"),
            accountsService = TestSolanaAccountsService("87vTugUvkkepa84mBRfENnvkPQRj5EZSkiG8XyFAhbQQ"),
        )

        val result = runBlocking {
            preloader.preloadTokenTransfer(
                ConfirmParams.Builder(
                    assetId = AssetId(Chain.Solana, "2b1kV6DkPAnxd5ixfnxCpjxmKwqjjaYmCZfHsFu24GXo"),
                    from = Account(Chain.Solana, "AGkXQZ9qm99xukisDUHvspWHESrcjs8Y4AmQQgef3BRh", ""),
                    amount = BigInteger.valueOf(10_000_000)
                )
                .transfer(destination = DestinationAddress("AGkXQZ9qm99xukisDUHvspWHESrcjs8Y4AmQQgef3BRh")) as ConfirmParams.TransferParams.Token
            )
        }
        assertEquals(BigInteger("300004992"), result.chainData.gasFee().amount)
        assertEquals("123", (result.chainData as SolanaSignerPreloader.SolanaChainData).blockhash)
        assertEquals(SolanaTokenProgramId.Token2022, (result.chainData as SolanaSignerPreloader.SolanaChainData).tokenProgram)
        assertEquals("87vTugUvkkepa84mBRfENnvkPQRj5EZSkiG8XyFAhbQQ", (result.chainData as SolanaSignerPreloader.SolanaChainData).recipientTokenAddress)
        assertEquals("87vTugUvkkepa84mBRfENnvkPQRj5EZSkiG8XyFAhbQQ", (result.chainData as SolanaSignerPreloader.SolanaChainData).senderTokenAddress)
    }
}