package com.gemwallet.android.blockchain.clients.cosmos

import com.gemwallet.android.blockchain.clients.cosmos.services.CosmosAccountsService
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.DestinationAddress
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.blockchain.cosmos.models.CosmosAccount
import com.wallet.core.blockchain.cosmos.models.CosmosAccountResponse
import com.wallet.core.blockchain.cosmos.models.CosmosBlock
import com.wallet.core.blockchain.cosmos.models.CosmosBlockResponse
import com.wallet.core.blockchain.cosmos.models.CosmosHeader
import com.wallet.core.blockchain.cosmos.models.CosmosInjectiveAccount
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.math.BigInteger

class TestCosmosPreload {

    private val accountsService = object  : CosmosAccountsService {

        var addressRequest: String = ""

        override suspend fun getAccountData(owner: String): Result<CosmosAccountResponse<CosmosAccount>> {
            addressRequest = owner
            return Result.success(CosmosAccountResponse(CosmosAccount(account_number = "2913388", sequence = "10")))
        }

        override suspend fun getInjectiveAccountData(owner: String): Result<CosmosAccountResponse<CosmosInjectiveAccount>> {
            throw Exception()
        }

        override suspend fun getNodeInfo(): Result<CosmosBlockResponse> {
            return Result.success(CosmosBlockResponse(CosmosBlock(CosmosHeader(chain_id = "osmosis-1", height = "25181150"))))
        }
    }


    @Test
    fun testCosmosPreload() {
        val preload = CosmosSignerPreloader(Chain.Osmosis, accountsService)
        val result = runBlocking {
            preload.preloadNativeTransfer(
                ConfirmParams.TransferParams.Native(
                    from = Account(Chain.Osmosis, "osmo1q0d0q8w8y8t6h4l9w5u8p8s8h8f8e8r8t8y8u8i8o8p8", ""),
                    amount = BigInteger.ONE,
                    assetId = AssetId(Chain.Osmosis),
                    destination = DestinationAddress("osmo1rcjvzz8wzktqfz8qjf0l9q45kzxvd0z0n7l5cf"),
                )
            )
        }
        assertEquals("osmo1q0d0q8w8y8t6h4l9w5u8p8s8h8f8e8r8t8y8u8i8o8p8", accountsService.addressRequest)

        assertEquals(BigInteger.valueOf(1), result.input.amount)
        assertEquals("osmo1q0d0q8w8y8t6h4l9w5u8p8s8h8f8e8r8t8y8u8i8o8p8", result.input.from.address)
        assertEquals(AssetId(Chain.Osmosis).toIdentifier(), result.input.assetId.toIdentifier())
        assertEquals(false, result.input.isMax())
        assertEquals("osmo1rcjvzz8wzktqfz8qjf0l9q45kzxvd0z0n7l5cf", result.input.destination()?.address)
        assertEquals(null, result.input.memo())

        assertEquals(BigInteger.valueOf(10000L), result.chainData.fee().amount)
        assertEquals(BigInteger.valueOf(10000L), (result.chainData.fee() as GasFee).maxGasPrice)
        assertEquals(BigInteger.valueOf(200000L), (result.chainData.fee() as GasFee).limit)
        assertEquals(AssetId(Chain.Osmosis).toIdentifier(), result.chainData.fee().feeAssetId.toIdentifier())
        assertEquals(TxSpeed.Normal, result.chainData.fee().speed)
        assertEquals(10L, (result.chainData as CosmosSignerPreloader.CosmosChainData).sequence)
        assertEquals(2913388L, (result.chainData as CosmosSignerPreloader.CosmosChainData).accountNumber)
        assertEquals("osmosis-1", (result.chainData as CosmosSignerPreloader.CosmosChainData).chainId)
    }

    @Test
    fun testCosmosDelegatePreload() {
        val preload = CosmosSignerPreloader(Chain.Osmosis, accountsService)
        val result = runBlocking {
            preload.preloadStake(
                ConfirmParams.Stake.DelegateParams(
                    from = Account(Chain.Osmosis, "osmo1q0d0q8w8y8t6h4l9w5u8p8s8h8f8e8r8t8y8u8i8o8p8", ""),
                    amount = BigInteger.ONE,
                    assetId = AssetId(Chain.Osmosis),
                    validatorId = "osmovaloper1pxphtfhqnx9ny27d53z4052e3r76e7qq495ehm"
                )
            )
        }

        assertEquals("osmo1q0d0q8w8y8t6h4l9w5u8p8s8h8f8e8r8t8y8u8i8o8p8", accountsService.addressRequest)
        assertEquals(BigInteger.valueOf(100000L), result.chainData.fee().amount)
        assertEquals(BigInteger.valueOf(100000L), (result.chainData.fee() as GasFee).maxGasPrice)
        assertEquals(BigInteger.valueOf(1000000L), (result.chainData.fee() as GasFee).limit)
        assertEquals(AssetId(Chain.Osmosis).toIdentifier(), result.chainData.fee().feeAssetId.toIdentifier())
        assertEquals(TxSpeed.Normal, result.chainData.fee().speed)
        assertEquals(10L, (result.chainData as CosmosSignerPreloader.CosmosChainData).sequence)
        assertEquals(2913388L, (result.chainData as CosmosSignerPreloader.CosmosChainData).accountNumber)
        assertEquals("osmosis-1", (result.chainData as CosmosSignerPreloader.CosmosChainData).chainId)
    }

    @Test
    fun testCosmosUndelegatePreload() {
        val preload = CosmosSignerPreloader(Chain.Osmosis, accountsService)
        val result = runBlocking {
            preload.preloadStake(
                ConfirmParams.Stake.UndelegateParams(
                    from = Account(Chain.Osmosis, "osmo1q0d0q8w8y8t6h4l9w5u8p8s8h8f8e8r8t8y8u8i8o8p8", ""),
                    amount = BigInteger.ONE,
                    assetId = AssetId(Chain.Osmosis),
                    validatorId = "osmovaloper1pxphtfhqnx9ny27d53z4052e3r76e7qq495ehm",
                    delegationId = "25053096",
                    share = "",
                    balance = "2000000",
                )
            )
        }
        assertEquals("osmo1q0d0q8w8y8t6h4l9w5u8p8s8h8f8e8r8t8y8u8i8o8p8", accountsService.addressRequest)
        assertEquals(BigInteger.valueOf(100000L), result.chainData.fee().amount)
        assertEquals(BigInteger.valueOf(100000L), (result.chainData.fee() as GasFee).maxGasPrice)
        assertEquals(BigInteger.valueOf(1000000L), (result.chainData.fee() as GasFee).limit)
        assertEquals(AssetId(Chain.Osmosis).toIdentifier(), result.chainData.fee().feeAssetId.toIdentifier())
        assertEquals(TxSpeed.Normal, result.chainData.fee().speed)
        assertEquals(10L, (result.chainData as CosmosSignerPreloader.CosmosChainData).sequence)
        assertEquals(2913388L, (result.chainData as CosmosSignerPreloader.CosmosChainData).accountNumber)
        assertEquals("osmosis-1", (result.chainData as CosmosSignerPreloader.CosmosChainData).chainId)
    }

    @Test
    fun testCosmosRedelegatePreload() {
        val preload = CosmosSignerPreloader(Chain.Osmosis, accountsService)
        val result = runBlocking {
            preload.preloadStake(
                ConfirmParams.Stake.RedelegateParams(
                    from = Account(Chain.Osmosis, "osmo1q0d0q8w8y8t6h4l9w5u8p8s8h8f8e8r8t8y8u8i8o8p8", ""),
                    amount = BigInteger.ONE,
                    assetId = AssetId(Chain.Osmosis),
                    srcValidatorId = "osmovaloper1pxphtfhqnx9ny27d53z4052e3r76e7qq495ehm",
                    dstValidatorId = "osmovaloper1z0sh4s80u99l6y9d3vfy582p8jejeeu6tcucs2",
                    share = "",
                    balance = "2000000",
                )
            )
        }
        assertEquals("osmo1q0d0q8w8y8t6h4l9w5u8p8s8h8f8e8r8t8y8u8i8o8p8", accountsService.addressRequest)
        assertEquals(BigInteger.valueOf(100000L), result.chainData.fee().amount)
        assertEquals(BigInteger.valueOf(100000L), (result.chainData.fee() as GasFee).maxGasPrice)
        assertEquals(BigInteger.valueOf(1250000L), (result.chainData.fee() as GasFee).limit)
        assertEquals(AssetId(Chain.Osmosis).toIdentifier(), result.chainData.fee().feeAssetId.toIdentifier())
        assertEquals(TxSpeed.Normal, result.chainData.fee().speed)
        assertEquals(10L, (result.chainData as CosmosSignerPreloader.CosmosChainData).sequence)
        assertEquals(2913388L, (result.chainData as CosmosSignerPreloader.CosmosChainData).accountNumber)
        assertEquals("osmosis-1", (result.chainData as CosmosSignerPreloader.CosmosChainData).chainId)
    }

    @Test
    fun testCosmosRewardsPreload() {
        val preload = CosmosSignerPreloader(Chain.Osmosis, accountsService)
        val result = runBlocking {
            preload.preloadStake(
                ConfirmParams.Stake.RewardsParams(
                    from = Account(Chain.Osmosis, "osmo1q0d0q8w8y8t6h4l9w5u8p8s8h8f8e8r8t8y8u8i8o8p8", ""),
                    amount = BigInteger.ONE,
                    assetId = AssetId(Chain.Osmosis),
                    validatorsId = listOf(
                        "osmovaloper1pxphtfhqnx9ny27d53z4052e3r76e7qq495ehm",
                        "osmovaloper1z0sh4s80u99l6y9d3vfy582p8jejeeu6tcucs2",
                    ),
                )
            )
        }
        assertEquals("osmo1q0d0q8w8y8t6h4l9w5u8p8s8h8f8e8r8t8y8u8i8o8p8", accountsService.addressRequest)
        assertEquals(BigInteger.valueOf(100000L), result.chainData.fee().amount)
        assertEquals(BigInteger.valueOf(100000L), (result.chainData.fee() as GasFee).maxGasPrice)
        assertEquals(BigInteger.valueOf(900000L), (result.chainData.fee() as GasFee).limit)
        assertEquals(AssetId(Chain.Osmosis).toIdentifier(), result.chainData.fee().feeAssetId.toIdentifier())
        assertEquals(TxSpeed.Normal, result.chainData.fee().speed)
        assertEquals(10L, (result.chainData as CosmosSignerPreloader.CosmosChainData).sequence)
        assertEquals(2913388L, (result.chainData as CosmosSignerPreloader.CosmosChainData).accountNumber)
        assertEquals("osmosis-1", (result.chainData as CosmosSignerPreloader.CosmosChainData).chainId)
    }
}