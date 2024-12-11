package com.gemwallet.android.blockchain.clients.evm

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gemwallet.android.blockchain.clients.ethereum.EvmSignClient
import com.gemwallet.android.blockchain.clients.ethereum.EvmSignerPreloader
import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.blockchain.testPhrase
import com.gemwallet.android.ext.asset
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.DestinationAddress
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet
import java.math.BigInteger

@RunWith(AndroidJUnit4::class)
class TestEthSign {

    companion object {
        init {
            includeLibs()
        }
    }

    val signClient = EvmSignClient(Chain.Ethereum)
    val privateKey = HDWallet(testPhrase, "").getKeyForCoin(CoinType.ETHEREUM).data()

    @Test
    fun test_Evm_sign_native() {
        val sign = runBlocking {
            signClient.signTransfer(
                params = SignerParams(
                    input = ConfirmParams.TransferParams.Native(
                        assetId = Chain.Ethereum.asset().id,
                        amount = BigInteger.TEN.pow(Chain.Ethereum.asset().decimals),
                        destination = DestinationAddress("0x9b1DB81180c31B1b428572Be105E209b5A6222b7"),
                        from = Account(Chain.Ethereum, "0x9b1DB81180c31B1b428572Be105E209b5A6222b7", "")
                    ),
                    finalAmount = BigInteger.TEN.pow(Chain.Ethereum.asset().decimals),
                    chainData = EvmSignerPreloader.EvmChainData(
                        chainId = "1",
                        nonce = BigInteger.ONE,
                        fee = GasFee(
                            maxGasPrice = BigInteger.TEN,
                            limit = BigInteger("21000"),
                            minerFee = BigInteger.TEN,
                            relay = BigInteger.TEN,
                            speed = TxSpeed.Normal,
                            feeAssetId = Chain.Ethereum.asset().id,
                        )
                    )
                ),
                txSpeed = TxSpeed.Normal,
                privateKey,
            )
        }
        assertEquals(
            "0x02f86a01010a0a825208949b1db81180c31b1b428572be105e209b5a6222b7880de0b6b3a76400008" +
                    "0c001a04936670cff2d450a1375fb2c42cf9f97130f9f9365197e4e8461a8c43fe24786a041" +
                    "833ce7835a78604c8518ef4dcef4e6dcd2e2d031dce759cd89790b6054fa07",
            sign.toHexString()
        )
    }

    @Test
    fun test_EvmTokenSign() {
        val sign = runBlocking {
            signClient.signTransfer(
                params = SignerParams(
                    input = ConfirmParams.TransferParams.Token(
                        assetId = AssetId(Chain.Ethereum, "0xdAC17F958D2ee523a2206206994597C13D831ec7"),
                        amount = BigInteger.TEN.pow(Chain.Ethereum.asset().decimals),
                        destination = DestinationAddress("0x9b1DB81180c31B1b428572Be105E209b5A6222b7"),
                        from = Account(Chain.Ethereum, "0x9b1DB81180c31B1b428572Be105E209b5A6222b7", "")
                    ),
                    finalAmount = BigInteger.TEN.pow(Chain.Ethereum.asset().decimals),
                    chainData = EvmSignerPreloader.EvmChainData(
                        chainId = "1",
                        nonce = BigInteger.ONE,
                        fee = GasFee(
                            maxGasPrice = BigInteger.TEN,
                            limit = BigInteger("91000"),
                            minerFee = BigInteger.TEN,
                            relay = BigInteger.TEN,
                            speed = TxSpeed.Normal,
                            feeAssetId = Chain.Ethereum.asset().id,
                        )
                    )
                ),
                txSpeed = TxSpeed.Normal,
                privateKey,
            )
        }
        assertEquals(
            "0x02f8a801010a0a8301637894dac17f958d2ee523a2206206994597c13d831ec780b844a9059cbb000" +
                    "0000000000000000000009b1db81180c31b1b428572be105e209b5a6222b700000000000000" +
                    "00000000000000000000000000000000000de0b6b3a7640000c001a02a975d0be8ce97d4518" +
                    "cd22407a21697f0177b8ca7c057b868eaba32aefd6887a00b20f74083ac147b7733c0b72d2f" +
                    "87cc96fc75be610da14b4f6265703421d273",
            sign.toHexString()
        )
    }

    @Test
    fun test_Evm_sign_swap() {
        val sign = runBlocking {
            signClient.signTransfer(
                params = SignerParams(
                    input = ConfirmParams.SwapParams(
                        fromAssetId = AssetId(Chain.Ethereum, "0xdAC17F958D2ee523a2206206994597C13D831ec7"),
                        toAssetId = AssetId(Chain.Ethereum, "0xdAC17F958D2ee523a2206206994597C13D831ec7"),
                        fromAmount = BigInteger.TEN.pow(Chain.Ethereum.asset().decimals),
                        toAmount = BigInteger.TEN.pow(Chain.Ethereum.asset().decimals),
                        swapData = "0xbc",
                        provider = "some_provide",
                        to = "0x9b1DB81180c31B1b428572Be105E209b5A6222b7",
                        value = "10",
                        from = Account(Chain.Ethereum, "0x9b1DB81180c31B1b428572Be105E209b5A6222b7", "")
                    ),
                    finalAmount = BigInteger.TEN.pow(Chain.Ethereum.asset().decimals),
                    chainData = EvmSignerPreloader.EvmChainData(
                        chainId = "1",
                        nonce = BigInteger.ONE,
                        fee = GasFee(
                            maxGasPrice = BigInteger.TEN,
                            limit = BigInteger("91000"),
                            minerFee = BigInteger.TEN,
                            relay = BigInteger.TEN,
                            speed = TxSpeed.Normal,
                            feeAssetId = Chain.Ethereum.asset().id,
                        )
                    )
                ),
                txSpeed = TxSpeed.Normal,
                privateKey,
            )
        }
        assertEquals(
            "0x02f86401010a0a83016378949b1db81180c31b1b428572be105e209b5a6222b70a81bcc001a084f6c" +
                    "708ff9bb9ef4f898860c38abf2293a9d34cf22e28047f5afa2af65b048ca06d436644b5fac5" +
                    "74f27c0caade1db8ee72a149897fcfe2f00797e78e8f58437a",
            sign.toHexString()
        )
    }

    @Test
    fun test_Evm_sign_delegate() {
        val sign = runBlocking {
            signClient.signTransfer(
                params = SignerParams(
                    input = ConfirmParams.Stake.DelegateParams(
                        assetId = AssetId(Chain.SmartChain),
                        amount = BigInteger.TEN,
                        from = Account(Chain.Ethereum, "0x9b1DB81180c31B1b428572Be105E209b5A6222b7", ""),
                        validatorId = "0x9941BCe2601fC93478DF9f5F6Cc83F4FFC1D71d8"
                    ),
                    finalAmount = BigInteger.TEN.pow(Chain.Ethereum.asset().decimals),
                    chainData = EvmSignerPreloader.EvmChainData(
                        chainId = "1",
                        nonce = BigInteger.ONE,
                        fee = GasFee(
                            maxGasPrice = BigInteger.TEN,
                            limit = BigInteger("91000"),
                            minerFee = BigInteger.TEN,
                            relay = BigInteger.TEN,
                            speed = TxSpeed.Normal,
                            feeAssetId = Chain.Ethereum.asset().id,
                        )
                    )
                ),
                txSpeed = TxSpeed.Normal,
                privateKey,
            )
        }
        assertEquals(
            "0x02f8b031010a0a83016378940000000000000000000000000000000000002002880de0b6b3a764000" +
                    "0b844982ef0a70000000000000000000000009941bce2601fc93478df9f5f6cc83f4ffc1d71" +
                    "d80000000000000000000000000000000000000000000000000000000000000000c001a0708" +
                    "ce0a221cdfccdaa992fab54ebf65f0e6bd2a319b679f16bad820a332e76d5a071a1409772887" +
                    "9ebedcd62fc9bc2b1e5ae82b509d8c9da665123db2df895a280",
            sign.toHexString()
        )
    }


    @Test
    fun test_Evm_sign_undelegate() {
        val sign = runBlocking {
            signClient.signTransfer(
                params = SignerParams(
                    input = ConfirmParams.Stake.UndelegateParams(
                        assetId = AssetId(Chain.SmartChain),
                        amount = BigInteger("1002901689671695193"),
                        from = Account(Chain.Ethereum, "0x9b1DB81180c31B1b428572Be105E209b5A6222b7", ""),
                        validatorId = "0x9941BCe2601fC93478DF9f5F6Cc83F4FFC1D71d8",
                        delegationId = "",
                        share = "991645728829172501",
                        balance = "1002901689671695193",
                    ),
                    finalAmount = BigInteger.TEN.pow(Chain.Ethereum.asset().decimals),
                    chainData = EvmSignerPreloader.EvmChainData(
                        chainId = "1",
                        nonce = BigInteger.ONE,
                        fee = GasFee(
                            maxGasPrice = BigInteger.TEN,
                            limit = BigInteger("91000"),
                            minerFee = BigInteger.TEN,
                            relay = BigInteger.TEN,
                            speed = TxSpeed.Normal,
                            feeAssetId = Chain.Ethereum.asset().id,
                        )
                    )
                ),
                txSpeed = TxSpeed.Normal,
                privateKey,
            )
        }
        assertEquals(
            "0x02f8a831010a0a8301637894000000000000000000000000000000000000200280b8444d99dd16000" +
                    "0000000000000000000009941bce2601fc93478df9f5f6cc83f4ffc1d71d800000000000000" +
                    "00000000000000000000000000000000000dc3088951e56b15c001a0e3fcddc355556d317b4" +
                    "c67b3171a9565ab3973e20c6a468933a0491824b7dce4a00cf2947749cd16b758ae7db408a2" +
                    "43b86b31239a595fbfd283541ad12872c7ac",
            sign.toHexString()
        )
    }

    @Test
    fun test_Evm_sign_approval() {
        val sign = runBlocking {
            signClient.signTransfer(
                params = SignerParams(
                    input = ConfirmParams.TokenApprovalParams(
                        assetId = AssetId(Chain.SmartChain, "0x0E09FaBB73Bd3Ade0a17ECC321fD13a19e81cE82"),
                        from = Account(Chain.SmartChain, "0x0Eb3a705fc54725037CC9e008bDede697f62F335", ""),
                        data = "0x095ea7b300000000000000000000000031c2f6fcff4f8759b3bd5bf0e1084a" +
                                "055615c7687ffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                                "fffffffffff",
                        provider = "Uniswap v3",
                        contract = "0x9b1DB81180c31B1b428572Be105E209b5A6222b7",
                    ),
                    finalAmount = BigInteger.ZERO,
                    chainData = EvmSignerPreloader.EvmChainData(
                        chainId = "1",
                        nonce = BigInteger.ONE,
                        fee = GasFee(
                            maxGasPrice = BigInteger.TEN,
                            limit = BigInteger("91000"),
                            minerFee = BigInteger.TEN,
                            relay = BigInteger.TEN,
                            speed = TxSpeed.Normal,
                            feeAssetId = Chain.SmartChain.asset().id,
                        )
                    )
                ),
                txSpeed = TxSpeed.Normal,
                privateKey,
            )
        }
        assertEquals(
            "0x02f8a801010a0a83016378940e09fabb73bd3ade0a17ecc321fd13a19e81ce8280b844095ea7b3000" +
                    "00000000000000000000031c2f6fcff4f8759b3bd5bf0e1084a055615c7687fffffffffffff" +
                    "ffffffffffffffffffffffffffffffffffffffffffffffffffc080a03a683902daf791be51b" +
                    "7354dbe5cef567c3825ce52808948ad00b195f79fb736a057f151181ea2898b4a6ef42e1f24" +
                    "0f64995e83fa19ade070e4d9ec7b0f66469c",
            sign.toHexString()
        )
    }
}