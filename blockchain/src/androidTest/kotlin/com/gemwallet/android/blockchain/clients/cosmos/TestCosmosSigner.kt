package com.gemwallet.android.blockchain.clients.cosmos

import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.blockchain.testPhrase
import com.gemwallet.android.ext.asset
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.DestinationAddress
import com.gemwallet.android.model.GasFee
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.DelegationBase
import com.wallet.core.primitives.DelegationState
import com.wallet.core.primitives.DelegationValidator
import com.wallet.core.primitives.FeePriority
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet
import java.math.BigInteger

class TestCosmosSigner {
    companion object {
        init {
            includeLibs()
        }
    }

    val osmoAccount = Account(Chain.Osmosis, "osmo1kglemumu8mn658j6g4z9jzn3zef2qdyyvklwa3", "")
    val signer = CosmosSignClient(Chain.Osmosis)
    val privateKey = HDWallet(testPhrase, "").getKeyForCoin(CoinType.OSMOSIS).data()

    // TODO: Add Swap and token transfer

    @Test
    fun testSignNativeTransfer() {
        val transfer = ConfirmParams.Builder(Chain.Osmosis.asset(), osmoAccount, BigInteger.TEN)
            .transfer(DestinationAddress("osmo1rcjvzz8wzktqfz8qjf0l9q45kzxvd0z0n7l5cf")) as ConfirmParams.TransferParams.Native
        val chainData = CosmosSignerPreloader.CosmosChainData(
            chainId = "osmosis-1",
            accountNumber = 2913388L,
            sequence = 10L,
            fee = GasFee(
                feeAssetId = AssetId(Chain.Osmosis),
                maxGasPrice = BigInteger.valueOf(10000L),
                limit = BigInteger.valueOf(200000L),
                amount = BigInteger.valueOf(10000L),
                priority = FeePriority.Normal,
            )
        )
        val finalAmount = BigInteger.TEN
        val result = runBlocking {
            signer.signNativeTransfer(transfer, chainData, finalAmount, FeePriority.Normal, privateKey)
        }.first().toHexString()
        assertEquals(
            "0x7b226d6f6465223a2242524f4144434153545f4d4f44455f53594e43222c2274785f6279746573223" +
                    "a22436f6f42436f6342436877765932397a6257397a4c6d4a68626d7375646a46695a585268" +
                    "4d53354e633264545a57356b456d634b4b32397a62573878613264735a57313162585534625" +
                    "734324e5468714e6d6330656a6c71656d347a656d566d4d6e466b65586c326132783359544d" +
                    "534b32397a62573878636d4e71646e70364f486436613352785a6e6f346357706d4d4777356" +
                    "354513161337034646d5177656a42754e3277315932596143776f466457397a62573853416a" +
                    "4577456d674b55417047436838765932397a6257397a4c6d4e79655842306279357a5a574e7" +
                    "74d6a5532617a4575554856695332563545694d4b49514d736c63596e374468506535622f38" +
                    "6c4d33466e50586847426a3553644331352b584931685a31675962424249454367494941526" +
                    "74b4568514b44676f466457397a62573853425445774d444177454d436144427041564a6b44" +
                    "786153355a6167686d4a365a7470433979696d374a413864754f384d774f4f44644a6548454" +
                    "87373483350514e2b34596c2b5356794c744e4557362b4944554b666b4731646649594f7670" +
                    "5269466c4f79673d3d227d",
            result
        )
    }

    @Test
    fun testSignStake() {
        val transfer = ConfirmParams.Builder(Chain.Osmosis.asset(), osmoAccount, BigInteger.TEN)
            .delegate("osmovaloper1pxphtfhqnx9ny27d53z4052e3r76e7qq495ehm")
        val chainData = CosmosSignerPreloader.CosmosChainData(
            chainId = "osmosis-1",
            accountNumber = 2913388L,
            sequence = 10L,
            fee = GasFee(
                feeAssetId = AssetId(Chain.Osmosis),
                maxGasPrice = BigInteger.valueOf(10000L),
                limit = BigInteger.valueOf(200000L),
                amount = BigInteger.valueOf(10000L),
                priority = FeePriority.Normal,
            )
        )
        val finalAmount = BigInteger.TEN
        val result = runBlocking {
            signer.signDelegate(transfer, chainData, finalAmount, FeePriority.Normal, privateKey)
        }.first().toHexString()
        assertEquals(
            "0x7b226d6f6465223a2242524f4144434153545f4d4f44455f53594e43222c2274785f6279746573223" +
                    "a22437134424370554243694d765932397a6257397a4c6e4e3059577470626d6375646a4669" +
                    "5a5852684d53354e633264455a57786c5a3246305a524a7543697476633231764d57746e624" +
                    "75674645731314f4731754e6a5534616a5a6e4e486f35616e70754d33706c5a6a4a785a486c" +
                    "35646d74736432457a456a4a7663323176646d46736233426c636a46776548426f64475a6f6" +
                    "35735344f5735354d6a646b4e544e364e4441314d6d557a636a63325a546478635451354e57" +
                    "566f62526f4c4367563162334e74627849434d54415346464e305957746c49485a705953424" +
                    "85a5730675632467362475630456d674b55417047436838765932397a6257397a4c6d4e7965" +
                    "5842306279357a5a574e774d6a5532617a4575554856695332563545694d4b49514d736c635" +
                    "96e374468506535622f386c4d33466e50586847426a3553644331352b584931685a31675962" +
                    "42424945436749494152674b4568514b44676f466457397a62573853425445774d444177454" +
                    "d43614442704178683975774e5a76716c32664f444345417034586875634f3163785859727a" +
                    "326f4d456b61742b77764a45503156446c6169345a6e4c7a2b6e396d5262676a46313433456" +
                    "673616f6e6f4568333675514b594f5775513d3d227d",
            result
        )
    }

    @Test
    fun testSignUndelegate() {
        val transfer = ConfirmParams.Builder(Chain.Osmosis.asset(), osmoAccount, BigInteger.TEN)
            .undelegate(
                Delegation(
                    base = DelegationBase(
                        assetId = AssetId(Chain.Osmosis),
                        state = DelegationState.Active,
                        balance = "10",
                        shares = "",
                        rewards = "",
                        completionDate = null,
                        delegationId = "25053096",
                        validatorId = "osmovaloper1pxphtfhqnx9ny27d53z4052e3r76e7qq495ehm"
                    ),
                    validator = DelegationValidator(
                        chain = Chain.Osmosis,
                        id = "osmovaloper1pxphtfhqnx9ny27d53z4052e3r76e7qq495ehm",
                        name = "",
                        isActive = true,
                        commision = 0.05,
                        apr = 5.11
                    )
                )
            )
        val chainData = CosmosSignerPreloader.CosmosChainData(
            chainId = "osmosis-1",
            accountNumber = 2913388L,
            sequence = 10L,
            fee = GasFee(
                feeAssetId = AssetId(Chain.Osmosis),
                maxGasPrice = BigInteger.valueOf(10000L),
                limit = BigInteger.valueOf(200000L),
                amount = BigInteger.valueOf(10000L),
                priority = FeePriority.Normal,
            )
        )
        val finalAmount = BigInteger.TEN
        val result = runBlocking {
            signer.signUndelegate(transfer, chainData, finalAmount, FeePriority.Normal, privateKey)
        }.first().toHexString()
        assertEquals(
            "0x7b226d6f6465223a2242524f4144434153545f4d4f44455f53594e43222c2274785f6279746573223" +
                    "a224372414243706342436955765932397a6257397a4c6e4e3059577470626d6375646a4669" +
                    "5a5852684d53354e63326456626d526c6247566e5958526c456d344b4b32397a62573878613" +
                    "264735a57313162585534625734324e5468714e6d6330656a6c71656d347a656d566d4d6e46" +
                    "6b65586c326132783359544d534d6d397a6257393259577876634756794d584234634768305" +
                    "a6d6878626e6735626e6b794e3251314d336f304d4455795a544e794e7a5a6c4e3346784e44" +
                    "6b315a5768744767734b4258567663323176456749784d4249555533526861325567646d6c6" +
                    "84945646c62534258595778735a58515361417051436b594b4879396a62334e7462334d7559" +
                    "334a35634852764c6e4e6c593341794e545a724d53355164574a4c5a586b5349776f6841797" +
                    "956786966734f4539376c762f79557a6357633965455947506c4a304c586e35636a57466e57" +
                    "426873454567514b4167674247416f5346416f4f4367563162334e74627849464d5441774d4" +
                    "44151774a6f4d476b436871792f4d33706248772f51766839796563536675706d3559525477" +
                    "654a513541706b433767384361383249626a2b574f7a59654858444f6276566d6f3144634d3" +
                    "27050652b2b6d4b426f3073686c424f43707869227d",
            result
        )
    }


    @Test
    fun testSignRedelegate() {
        val transfer = ConfirmParams.Builder(Chain.Osmosis.asset(), osmoAccount, BigInteger.TEN)
            .redelegate(
                "osmovaloper1z0sh4s80u99l6y9d3vfy582p8jejeeu6tcucs2",
                Delegation(
                    base = DelegationBase(
                        assetId = AssetId(Chain.Osmosis),
                        state = DelegationState.Active,
                        balance = "10",
                        shares = "",
                        rewards = "",
                        completionDate = null,
                        delegationId = "25053096",
                        validatorId = "osmovaloper1pxphtfhqnx9ny27d53z4052e3r76e7qq495ehm"
                    ),
                    validator = DelegationValidator(
                        chain = Chain.Osmosis,
                        id = "osmovaloper1pxphtfhqnx9ny27d53z4052e3r76e7qq495ehm",
                        name = "",
                        isActive = true,
                        commision = 0.05,
                        apr = 5.11
                    )
                )
            )
        val chainData = CosmosSignerPreloader.CosmosChainData(
            chainId = "osmosis-1",
            accountNumber = 2913388L,
            sequence = 10L,
            fee = GasFee(
                feeAssetId = AssetId(Chain.Osmosis),
                maxGasPrice = BigInteger.valueOf(10000L),
                limit = BigInteger.valueOf(200000L),
                amount = BigInteger.valueOf(10000L),
                priority = FeePriority.Normal,
            )
        )
        val finalAmount = BigInteger.TEN
        val result = runBlocking {
            signer.signRedelegate(transfer, chainData, finalAmount, FeePriority.Normal, privateKey)
        }.first().toHexString()
        assertEquals(
            "0x7b226d6f6465223a2242524f4144434153545f4d4f44455f53594e43222c2274785f6279746573223" +
                    "a2243756f424374454243696f765932397a6257397a4c6e4e3059577470626d6375646a4669" +
                    "5a5852684d53354e633264435a576470626c4a6c5a4756735a576468644755536f67454b4b3" +
                    "2397a62573878613264735a57313162585534625734324e5468714e6d6330656a6c71656d34" +
                    "7a656d566d4d6e466b65586c326132783359544d534d6d397a6257393259577876634756794" +
                    "d584234634768305a6d6878626e6735626e6b794e3251314d336f304d4455795a544e794e7a" +
                    "5a6c4e3346784e446b315a576874476a4a7663323176646d46736233426c636a46364d484e6" +
                    "f4e484d344d4855354f57773265546c6b4d335a6d655455344d6e4134616d56715a5756314e" +
                    "6e526a64574e7a4d69494c4367563162334e74627849434d54415346464e305957746c49485" +
                    "a70595342485a5730675632467362475630456d674b55417047436838765932397a6257397a" +
                    "4c6d4e79655842306279357a5a574e774d6a5532617a4575554856695332563545694d4b495" +
                    "14d736c63596e374468506535622f386c4d33466e50586847426a3553644331352b58493168" +
                    "5a3167596242424945436749494152674b4568514b44676f466457397a62573853425445774" +
                    "d444177454d436144427041625637384c536c433373734c2f507a373335594d6b6c52614a4a" +
                    "4f54684a57645139324e33767a6730566c346f6e55513661666e7177312b70353937756d467" +
                    "837674a76364752414c566874722b4b6162395a3231673d3d227d",
            result
        )
    }

    @Test
    fun testSignRewards() {
        val transfer = ConfirmParams.Builder(Chain.Osmosis.asset(), osmoAccount, BigInteger.TEN)
            .rewards(
                listOf(
                    "osmovaloper1pxphtfhqnx9ny27d53z4052e3r76e7qq495ehm",
                    "osmovaloper1pxphtfhqnx9ny27d53z4052e3r76e7qq495ehm",
                ),
            )
        val chainData = CosmosSignerPreloader.CosmosChainData(
            chainId = "osmosis-1",
            accountNumber = 2913388L,
            sequence = 10L,
            fee = GasFee(
                feeAssetId = AssetId(Chain.Osmosis),
                maxGasPrice = BigInteger.valueOf(10000L),
                limit = BigInteger.valueOf(200000L),
                amount = BigInteger.valueOf(10000L),
                priority = FeePriority.Normal,
            )
        )
        val finalAmount = BigInteger.TEN
        val result = runBlocking {
            signer.signRewards(transfer, chainData, finalAmount, FeePriority.Normal, privateKey)
        }.first().toHexString()
        assertEquals(
            "0x7b226d6f6465223a2242524f4144434153545f4d4f44455f53594e43222c2274785f6279746573223" +
                    "a224374514343707742436a63765932397a6257397a4c6d52706333527961574a3164476c76" +
                    "626935324d574a6c644745784c6b317a5a3164706447686b636d4633524756735a576468644" +
                    "73979556d563359584a6b456d454b4b32397a62573878613264735a57313162585534625734" +
                    "324e5468714e6d6330656a6c71656d347a656d566d4d6e466b65586c326132783359544d534" +
                    "d6d397a6257393259577876634756794d584234634768305a6d6878626e6735626e6b794e32" +
                    "51314d336f304d4455795a544e794e7a5a6c4e3346784e446b315a57687443707742436a637" +
                    "65932397a6257397a4c6d52706333527961574a3164476c76626935324d574a6c644745784c" +
                    "6b317a5a3164706447686b636d4633524756735a57646864473979556d563359584a6b456d4" +
                    "54b4b32397a62573878613264735a57313162585534625734324e5468714e6d6330656a6c71" +
                    "656d347a656d566d4d6e466b65586c326132783359544d534d6d397a6257393259577876634" +
                    "756794d584234634768305a6d6878626e6735626e6b794e3251314d336f304d4455795a544e" +
                    "794e7a5a6c4e3346784e446b315a57687445685254644746725a53423261574567523256744" +
                    "94664686247786c64424a6f436c414b52676f664c324e76633231766379356a636e6c776447" +
                    "38756332566a634449314e6d73784c6c4231596b746c6552496a436945444c4a58474a2b773" +
                    "4543375572f2f4a544e785a7a31345267592b556e517465666c794e59576459474777515342" +
                    "416f4343414559436849554367344b4258567663323176456755784d4441774d42434174526" +
                    "76151482f553930754348307a78394164592b414c49484d35615a316372425377597a655a5a" +
                    "656a623572576a454d56585253636a4f66766e67333358466e464864493445707039796b4e4" +
                    "e745156557739424a6e5a7368553d227d",
            result
        )
    }
}