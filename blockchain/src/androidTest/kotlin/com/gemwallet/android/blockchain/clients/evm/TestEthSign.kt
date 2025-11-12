package com.gemwallet.android.blockchain.clients.evm

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gemwallet.android.blockchain.clients.ethereum.EvmChainData
import com.gemwallet.android.blockchain.clients.ethereum.EvmSignClient
import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.blockchain.testPhrase
import com.gemwallet.android.ext.asset
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.DestinationAddress
import com.gemwallet.android.model.GasFee
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.DelegationBase
import com.wallet.core.primitives.DelegationState
import com.wallet.core.primitives.DelegationValidator
import com.wallet.core.primitives.FeePriority
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import uniffi.gemstone.GemSwapQuoteDataType
import uniffi.gemstone.SwapperProvider
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
            signClient.signNativeTransfer(
                params = ConfirmParams.TransferParams.Native(
                    asset = Chain.Ethereum.asset(),
                    amount = BigInteger.TEN.pow(Chain.Ethereum.asset().decimals),
                    destination = DestinationAddress("0x9b1DB81180c31B1b428572Be105E209b5A6222b7"),
                    from = Account(Chain.Ethereum, "0x9b1DB81180c31B1b428572Be105E209b5A6222b7", "")
                ),
                chainData = EvmChainData(
                    chainId = 1,
                    nonce = BigInteger.ONE,
                    stakeData = null,
                ),
                finalAmount = BigInteger.TEN.pow(Chain.Ethereum.asset().decimals),
                fee = GasFee(
                    maxGasPrice = BigInteger.TEN,
                    limit = BigInteger("21000"),
                    minerFee = BigInteger.TEN,
                    relay = BigInteger.TEN,
                    priority = FeePriority.Normal,
                    feeAssetId = Chain.Ethereum.asset().id,
                ),
                privateKey,
            )
        }
        assertEquals(
            "0x30326638366130313031306130613832353230383934396231646238313138306333316" +
                    "2316234323835373262653130356532303962356136323232623738383064653062366233613" +
                    "7363430303030383063303031613034393336363730636666326434353061313337356662326" +
                    "3343263663966393731333066396639333635313937653465383436316138633433666532343" +
                    "7383661303431383333636537383335613738363034633835313865663464636566346536646" +
                    "3643265326430333164636537353963643839373930623630353466613037",
            sign.first().toHexString()
        )
    }

    @Test
    fun test_EvmTokenSign() {
        val sign = runBlocking {
            signClient.signTokenTransfer(
                params = ConfirmParams.TransferParams.Token(
                    Asset(
                        AssetId(Chain.Ethereum, "0xdAC17F958D2ee523a2206206994597C13D831ec7"),
                        name = "USDT",
                        symbol = "USDT",
                        decimals = 8,
                        type = AssetType.ERC20,
                    ),
                    amount = BigInteger.TEN.pow(Chain.Ethereum.asset().decimals),
                    destination = DestinationAddress("0x9b1DB81180c31B1b428572Be105E209b5A6222b7"),
                    from = Account(Chain.Ethereum, "0x9b1DB81180c31B1b428572Be105E209b5A6222b7", "")
                ),
                chainData = EvmChainData(
                    chainId = 1,
                    nonce = BigInteger.ONE,
                    stakeData = null,
                ),
                finalAmount = BigInteger.TEN.pow(Chain.Ethereum.asset().decimals),
                fee = GasFee(
                    maxGasPrice = BigInteger.TEN,
                    limit = BigInteger("91000"),
                    minerFee = BigInteger.TEN,
                    relay = BigInteger.TEN,
                    priority = FeePriority.Normal,
                    feeAssetId = Chain.Ethereum.asset().id,
                ),
                privateKey,
            )
        }
        assertEquals(
            "0x30326638613830313031306130613833303136333738393464616331376639353864326" +
                    "5653532336132323036323036393934353937633133643833316563373830623834346139303" +
                    "5396362623030303030303030303030303030303030303030303030303962316462383131383" +
                    "0633331623162343238353732626531303565323039623561363232326237303030303030303" +
                    "0303030303030303030303030303030303030303030303030303030303030303030303030303" +
                    "0303030646530623662336137363430303030633030316130326139373564306265386365393" +
                    "7643435313863643232343037613231363937663031373762386361376330353762383638656" +
                    "1626133326165666436383837613030623230663734303833616331343762373733336330623" +
                    "7326432663837636339366663373562653631306461313462346636323635373033343231643" +
                    "23733",
            sign.first().toHexString()
        )
    }

    @Test
    fun test_Evm_sign_swap() {
        val sign = runBlocking {
            signClient.signSwap(
                params = ConfirmParams.SwapParams(
                    fromAsset = Asset(
                        AssetId(Chain.Ethereum, "0xdAC17F958D2ee523a2206206994597C13D831ec7"),
                        name = "USDT",
                        symbol = "USDT",
                        decimals = 8,
                        type = AssetType.ERC20,
                    ),
                    toAsset = Asset(
                        AssetId(Chain.Ethereum, "0xdAC17F958D2ee523a2206206994597C13D831ec7"),
                        name = "SMT",
                        symbol = "SMT",
                        decimals = 8,
                        type = AssetType.ERC20,
                    ),
                    fromAmount = BigInteger.TEN.pow(Chain.Ethereum.asset().decimals),
                    toAmount = BigInteger.TEN.pow(Chain.Ethereum.asset().decimals),
                    swapData = "0xbc",
                    protocol = "some_provide",
                    providerId = SwapperProvider.PANCAKESWAP_V3,
                    protocolId = "some_provide",
                    toAddress = "0x9b1DB81180c31B1b428572Be105E209b5A6222b7",
                    value = "10",
                    from = Account(Chain.Ethereum, "0x9b1DB81180c31B1b428572Be105E209b5A6222b7", ""),
                    providerName = "",
                    slippageBps = 0U,
                    etaInSeconds = 0U,
                    memo = "",
                    dataType = GemSwapQuoteDataType.CONTRACT,
                ),
                chainData = EvmChainData(
                    chainId = 1,
                    nonce = BigInteger.ONE,
                    stakeData = null,
                ),
                finalAmount = BigInteger.TEN.pow(Chain.Ethereum.asset().decimals),
                fee = GasFee(
                    maxGasPrice = BigInteger.TEN,
                    limit = BigInteger("91000"),
                    minerFee = BigInteger.TEN,
                    relay = BigInteger.TEN,
                    priority = FeePriority.Normal,
                    feeAssetId = Chain.Ethereum.asset().id,
                ),
                privateKey,
            )
        }
        assertEquals(
            "0x30326638363430313031306130613833303136333738393439623164623831313830633" +
                    "3316231623432383537326265313035653230396235613632323262373061383162636330303" +
                    "1613038346636633730386666396262396566346638393838363063333861626632323933613" +
                    "9643334636632326532383034376635616661326166363562303438636130366434333636343" +
                    "4623566616335373466323763306361616465316462386565373261313439383937666366653" +
                    "2663030373937653738653866353834333761",
            sign.first().toHexString()
        )
    }

    @Test
    fun test_Evm_sign_approval() {
        val sign = runBlocking {
            signClient.signTokenApproval(
                params = ConfirmParams.TokenApprovalParams(
                    Asset(
                        AssetId(Chain.SmartChain, "0x0E09FaBB73Bd3Ade0a17ECC321fD13a19e81cE82"),
                        name = "USDT",
                        symbol = "USDT",
                        decimals = 8,
                        type = AssetType.ERC20,
                    ),
                    from = Account(Chain.SmartChain, "0x0Eb3a705fc54725037CC9e008bDede697f62F335", ""),
                    data = "0x095ea7b300000000000000000000000031c2f6fcff4f8759b3bd5bf0e1084a" +
                            "055615c7687ffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                            "fffffffffff",
                    provider = "Uniswap v3",
                    contract = "0x9b1DB81180c31B1b428572Be105E209b5A6222b7",
                ),
                chainData = EvmChainData(
                    chainId = 1,
                    nonce = BigInteger.ONE,
                    stakeData = null,
                ),
                finalAmount = BigInteger.ZERO,
                fee = GasFee(
                    maxGasPrice = BigInteger.TEN,
                    limit = BigInteger("91000"),
                    minerFee = BigInteger.TEN,
                    relay = BigInteger.TEN,
                    priority = FeePriority.Normal,
                    feeAssetId = Chain.SmartChain.asset().id,
                ),
                privateKey,
            )
        }
        assertEquals(
            "0x30326638613830313031306130613833303136333738393430653039666162623733626" +
                    "4336164653061313765636333323166643133613139653831636538323830623834343039356" +
                    "5613762333030303030303030303030303030303030303030303030303331633266366663666" +
                    "6346638373539623362643562663065313038346130353536313563373638376666666666666" +
                    "6666666666666666666666666666666666666666666666666666666666666666666666666666" +
                    "6666666666666666666666666666666666666633038306130336136383339303264616637393" +
                    "1626535316237333534646265356365663536376333383235636535323830383934386164303" +
                    "0623139356637396662373336613035376631353131383165613238393862346136656634326" +
                    "5316632343066363439393565383366613139616465303730653464396563376230663636343" +
                    "63963",
            sign.first().toHexString()
        )
    }
}