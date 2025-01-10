package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.blockchain.testPhrase
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.DestinationAddress
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.SolanaTokenProgramId
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet
import java.math.BigInteger

class TestSolanaSigner {
    companion object {
        init {
            includeLibs()
        }
    }

    val privateKey = HDWallet(testPhrase, "").getKeyForCoin(CoinType.SOLANA).data()

    @Test
    fun testSolana_native_transfer() {
        val signer = SolanaSignClient(
            chain = Chain.Solana,
            getAsset = {
                Asset(
                    it,
                    name = "sol_asset",
                    symbol = "sol_asset",
                    decimals = 6,
                    type = AssetType.SPL,
                )
            }
        )
        val input = SignerParams(
            input = ConfirmParams.Builder(
                assetId = AssetId(Chain.Solana),
                from = Account(Chain.Solana, "4Yu2e1Wz5T1Ci2hAPswDqvMgSnJ1Ftw7ZZh8x7xKLx7S", ""),
                amount = BigInteger.valueOf(10_000_000)
            )
            .transfer(destination = DestinationAddress("4Yu2e1Wz5T1Ci2hAPswDqvMgSnJ1Ftw7ZZh8x7xKLx7S")) as ConfirmParams.TransferParams.Native,
            chainData = SolanaSignerPreloader.SolanaChainData(
                blockhash = "kiEPF6aKvEsj5nbi4FBvgRRm9ha36Y3cgDU9qnUKt32",
                fees = GasFee(
                    amount = BigInteger("105005000"),
                    minerFee = BigInteger("1050000000"),
                    maxGasPrice = BigInteger("5000"),
                    limit = BigInteger("100000"),
                    feeAssetId = AssetId(Chain.Solana),
                    speed = TxSpeed.Normal,
                ),
                recipientTokenAddress = null,
                senderTokenAddress = "",
                tokenProgram = SolanaTokenProgramId.Token
            )
        )
        val result = runBlocking { signer.signTransfer(input, TxSpeed.Normal, privateKey) }
        assertEquals("0x4159436b734f696556323339774436566c6841614a41464844544a647a632f61577a3331" +
                "6f693676686e783978676d4c544a5372643165504634454e73734a704867667575424e6e55556b3" +
                "159304a52784e504a59513442414149455365626b44466a2b415242396b4b486b394f4167745057" +
                "456e614370426a475a3869707a61372f4e43577330767554324f647746546758414767305a61753" +
                "17474703157354f7153437a77434c53384e5738357a71514d47526d2f6c495263792f2b7974756e" +
                "4c446d2b65386a4f573778666353617978446d7a704141414141414141414141414141414141414" +
                "141414141414141414141414141414141414141414141414141414141414c4d706730536d427863" +
                "684e486e756872566468464259677863634c722b5370616932436959444a4b51514d4341416b446" +
                "74c71565067414141414143414155436f49594241414d434141454d416741414141414141414141" +
                "41414141", result.toHexString())
    }

    @Test
    fun testSolana_token_transfer() {
        val signer = SolanaSignClient(
            chain = Chain.Solana,
            getAsset = {
                Asset(
                    it,
                    name = "sol_asset",
                    symbol = "sol_asset",
                    decimals = 6,
                    type = AssetType.SPL,
                )
            }
        )
        val input = SignerParams(
            input = ConfirmParams.Builder(
                assetId = AssetId(Chain.Solana, "Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB"),
                from = Account(Chain.Solana, "4Yu2e1Wz5T1Ci2hAPswDqvMgSnJ1Ftw7ZZh8x7xKLx7S", ""),
                amount = BigInteger.valueOf(10_000_000)
            )
            .transfer(destination = DestinationAddress("AGkXQZ9qm99xukisDUHvspWHESrcjs8Y4AmQQgef3BRh")) as ConfirmParams.TransferParams.Token,
            chainData = SolanaSignerPreloader.SolanaChainData(
                blockhash = "kiEPF6aKvEsj5nbi4FBvgRRm9ha36Y3cgDU9qnUKt32",
                fees = GasFee(
                    amount = BigInteger("105005000"),
                    minerFee = BigInteger("1050000000"),
                    maxGasPrice = BigInteger("5000"),
                    limit = BigInteger("100000"),
                    feeAssetId = AssetId(Chain.Solana),
                    speed = TxSpeed.Normal,
                ),
                recipientTokenAddress = "DVWPV7brSbPDkA7a3qdn6UJsVc3J3DyhQhjNaZeZqwzo",
                senderTokenAddress = "DVWPV7brSbPDkA7a3qdn6UJsVc3J3DyhQhjNaZeZqwzo",
                tokenProgram = SolanaTokenProgramId.Token
            )
        )
        val result = runBlocking { signer.signTransfer(input, TxSpeed.Normal, privateKey) }
        assertEquals("0x416238786c7268464374766e72677357477874416f6b6852423654737057545862643066" +
                "6e31557835466c736b734b317838714d6668326e313662454d384d6b6a4a4d467450664b7046684" +
                "25a376576716557713051514241414d465365626b44466a2b415242396b4b486b394f4167745057" +
                "456e614370426a475a3869707a61372f4e435775356d6277492b744e3454586473757149654b725" +
                "254647a734b644444707a385843635179334a766a50304d3442446d43763762496e4637316a4753" +
                "395546466f2f6c6c6f7a75344c5378774b657373346549494a6b41775a47622b5568467a4c2f374" +
                "b323663734f623537794d3562764639784a724c454f624f6b41414141414733666268313257686b" +
                "396e4c3455624f36336d73484c5346375639624e3545366a50574666763841715173796d44524b5" +
                "948467945306565364774563245554669444678777576354b6c714c594b4a674d6b704241774d41" +
                "43514f417570552b4141414141414d4142514b67686745414241514241674541436777414141414" +
                "1414141414141593d", result.toHexString())
    }

    @Test
    fun testSolana_token2022_transfer() {
        val signer = SolanaSignClient(
            chain = Chain.Solana,
            getAsset = {
                Asset(
                    it,
                    name = "sol_asset",
                    symbol = "sol_asset",
                    decimals = 6,
                    type = AssetType.SPL,
                )
            }
        )
        val input = SignerParams(
            input = ConfirmParams.Builder(
                assetId = AssetId(Chain.Solana, "2b1kV6DkPAnxd5ixfnxCpjxmKwqjjaYmCZfHsFu24GXo"),
                from = Account(Chain.Solana, "4Yu2e1Wz5T1Ci2hAPswDqvMgSnJ1Ftw7ZZh8x7xKLx7S", ""),
                amount = BigInteger.valueOf(10_000_000)
            )
            .transfer(destination = DestinationAddress("AGkXQZ9qm99xukisDUHvspWHESrcjs8Y4AmQQgef3BRh")) as ConfirmParams.TransferParams.Token,
            chainData = SolanaSignerPreloader.SolanaChainData(
                blockhash = "kiEPF6aKvEsj5nbi4FBvgRRm9ha36Y3cgDU9qnUKt32",
                fees = GasFee(
                    amount = BigInteger("105005000"),
                    minerFee = BigInteger("1050000000"),
                    maxGasPrice = BigInteger("5000"),
                    limit = BigInteger("100000"),
                    feeAssetId = AssetId(Chain.Solana),
                    speed = TxSpeed.Normal,
                ),
                recipientTokenAddress = "87vTugUvkkepa84mBRfENnvkPQRj5EZSkiG8XyFAhbQQ",
                senderTokenAddress = "87vTugUvkkepa84mBRfENnvkPQRj5EZSkiG8XyFAhbQQ",
                tokenProgram = SolanaTokenProgramId.Token2022
            )
        )
        val result = runBlocking { signer.signTransfer(input, TxSpeed.Normal, privateKey) }
        assertEquals("0x4152744d346e59365642416d427563754c675a7759623165304764516d6b782b6251764f" +
                "6241657558466e6a4b5a4744507930653972305249444f384757705056384439453345754b33416" +
                "e492b354b6b6d72485667414241414d465365626b44466a2b415242396b4b486b394f4167745057" +
                "456e614370426a475a3869707a61372f4e43577470783737467363375a7773776d4234324a65393" +
                "04538487a622b59486131534a74465451777a385351705265535344747369697148743063646755" +
                "2b566b666b355849514b6e4f505a394e57366654704c696e536541775a47622b5568467a4c2f374" +
                "b323663734f623537794d3562764639784a724c454f624f6b41414141414733666268376e575033" +
                "68684358627a6b624d33617468723854594f354453662b76666b6f324b474c2f4173796d44524b5" +
                "948467945306565364774563245554669444678777576354b6c714c594b4a674d6b704241774d41" +
                "43514f417570552b4141414141414d4142514b67686745414241514241674541436777414141414" +
                "1414141414141593d", result.toHexString())
    }

//    @Test
//    fun testSolana_swap() {
//        val signer = SolanaSignClient(
//            chain = Chain.Solana,
//            getAsset = {
//                Asset(
//                    it,
//                    name = "sol_asset",
//                    symbol = "sol_asset",
//                    decimals = 6,
//                    type = AssetType.SPL,
//                )
//            }
//        )
//        val input = SignerParams(
//            input = ConfirmParams.SwapParams(
//                from = Account(Chain.Solana, "4Yu2e1Wz5T1Ci2hAPswDqvMgSnJ1Ftw7ZZh8x7xKLx7S", ""),
//                fromAmount = BigInteger("1000000000"),
//                fromAssetId = AssetId(Chain.Solana),
//                provider = "Jupiter",
//                swapData = "AQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACAAQAHDjS+5PY53AVOBcAaDRlq7W22nVbk6pILPAItLw1bznOpVnc0WHX0iBa76mPlR5DMysds1h9/FNOv+q/dzEjiSqRmf8tThVI2sRkmD2EmMS9/AyOY0sK8kOkTIbTMftjXJHEzfZHfK+df8cHOiMH18Ck85+5FYvbuPgRfoH+q0xHdfiDLDpwNUu6Ja8lBL3sFhPDWgrrr6cu9Ez6m7wuAwlSpqhQZW2JKxscg7kplpPWG6E/FUSFLmKyo8J5QdY2PRdG4FzZMmQ0vu3MSk+s026EyyxDNxq/8Jesxeb8z3xsbAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADBkZv5SEXMv/srbpyw5vnvIzlu8X3EmssQ5s6QAAAAAR51VvyMcBu7nTFbs5oFQf9sbLeo/SOUQKxzaJWvBOPBt324ddloZPZy+FGzut5rBy0he1fWzeROoz1hX7/AKmMlyWPTiSJ8bs9ECkUjg2DC1oTmdr/EIQEjnvY2+n4WZqAC/9MhzaIlsIPwUBz6/HLWqN1/oH+Tb3IK6Tft154tD/6J/XX9kp0wJsfKVh53ksJqzbfyd1RSzIap7OM5ejXbCeYNe17/CeC+wmOilA+gAKngqcevwZzbvcwTpb9tggIAAUCwFwVAAgACQMWcwUAAAAAAAsGAAQAFQcKAQEHAgAEDAIAAAAAypo7AAAAAAoBBAERCwYABgAbBwoBAQkjCgwABAMBBhUbBQkNCRwMEhQTAwIKHRcMAgEQDw4RGhYZGAopwSCbM0HWnIECAgAAAD0AZAABOGQBAgDKmjsAAAAArghIDQAAAAAKADIKAwQAAAEJAgANKUgaezLclq6i6/fo4RGyOuqlNL8whAoKN2dzwqWgBMnIzccHAXbLxXfK206Eki7kKWIPQ9PpE4DMKW/o9pK4tDRe+0dG6+DVAZB1AwMFAgIBAA==",
//                to = "JUP6LkbZbjS1jKKwapdHNy74zcZ3tLUZoi5QNyVTaV4",
//                toAmount = BigInteger("222824622"),
//                toAssetId = AssetId(Chain.Solana, "Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB"),
//                value = "",
//            ),
//            chainData = SolanaSignerPreloader.SolanaChainData(
//                blockhash = "489EdRewv3Yk17rwq4HoVwfwPS3h291TmsVgdxDgFbhg",
//                fee = GasFee(
//                    amount = BigInteger("355000"),
//                    minerFee = BigInteger("250000"),
//                    maxGasPrice = BigInteger("5000"),
//                    limit = BigInteger("1400000"),
//                    feeAssetId = AssetId(Chain.Solana),
//                    speed = TxSpeed.Normal,
//                    options = mapOf("tokenAccountCreation" to BigInteger("2039280"))
//                ),
//                recipientTokenAddress = null,
//                senderTokenAddress = "",
//                tokenProgram = SolanaTokenProgramId.Token
//            )
//        )
//        val result = runBlocking { signer.signTransfer(input, TxSpeed.Normal, privateKey) }
//        assertEquals("", result.toHexString())
//    }
}