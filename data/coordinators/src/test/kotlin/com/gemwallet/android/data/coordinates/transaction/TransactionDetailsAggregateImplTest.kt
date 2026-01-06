package com.gemwallet.android.data.coordinates.transaction

import com.gemwallet.android.domains.transaction.values.TransactionDetailsValue
import com.gemwallet.android.model.AssetBalance
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Transaction
import com.gemwallet.android.model.TransactionExtended
import com.gemwallet.android.serializer.jsonEncoder
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.NFTAsset
import com.wallet.core.primitives.NFTImages
import com.wallet.core.primitives.NFTResource
import com.wallet.core.primitives.NFTType
import com.wallet.core.primitives.Price
import com.wallet.core.primitives.SwapProvider
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionSwapMetadata
import com.wallet.core.primitives.TransactionType
import org.junit.Assert
import org.junit.Test

class TransactionDetailsAggregateImplTest {

    private val btcAsset = Asset(
        id = AssetId(Chain.Bitcoin),
        name = "Bitcoin",
        symbol = "BTC",
        decimals = 8,
        type = AssetType.NATIVE,
    )

    private val ethAsset = Asset(
        id = AssetId(Chain.Ethereum),
        name = "Ethereum",
        symbol = "ETH",
        decimals = 18,
        type = AssetType.NATIVE,
    )

    private val usdtAsset = Asset(
        id = AssetId(Chain.Ethereum, "0xdac17f958d2ee523a2206206994597c13d831ec7"),
        name = "Tether",
        symbol = "USDT",
        decimals = 6,
        type = AssetType.ERC20,
    )

    private fun createTransaction(
        id: String = "tx123",
        assetId: AssetId = btcAsset.id,
        from: String = "bc1qsender",
        to: String = "bc1qreceiver",
        type: TransactionType = TransactionType.Transfer,
        state: TransactionState = TransactionState.Confirmed,
        direction: TransactionDirection = TransactionDirection.Outgoing,
        value: String = "100000000",
        fee: String = "1000",
        metadata: String? = null,
        memo: String? = null,
    ) = Transaction(
        id = id,
        assetId = assetId,
        from = from,
        to = to,
        contract = null,
        type = type,
        state = state,
        blockNumber = "123456",
        sequence = null,
        fee = fee,
        feeAssetId = assetId,
        value = value,
        memo = memo,
        direction = direction,
        utxoInputs = null,
        utxoOutputs = null,
        metadata = metadata,
        createdAt = 1767694414000,
    )

    private fun createTransactionExtended(
        transaction: Transaction,
        asset: Asset = btcAsset,
        feeAsset: Asset = asset,
        price: Price? = null,
        feePrice: Price? = null,
        assets: List<Asset> = emptyList(),
    ) = TransactionExtended(
        transaction = transaction,
        asset = asset,
        feeAsset = feeAsset,
        price = price,
        feePrice = feePrice,
        assets = assets,
    )

    private fun createAssetInfo(asset: Asset) = AssetInfo(
        owner = null,
        asset = asset,
        balance = AssetBalance(asset),
        walletId = null,
    )

    private fun createAggregate(
        data: TransactionExtended,
        associatedAssets: List<AssetInfo> = emptyList(),
        currency: Currency = Currency.USD,
    ) = TransactionDetailsAggregateImpl(
        data = data,
        associatedAssets = associatedAssets,
        explorer = TransactionDetailsValue.Explorer("https://example.com", "Explorer"),
        currency = currency,
    )

    @Test
    fun testBasicProperties() {
        val transaction = createTransaction(id = "test-id-123")
        val extended = createTransactionExtended(transaction, asset = btcAsset)
        val aggregate = createAggregate(extended)

        Assert.assertEquals("test-id-123", aggregate.id)
        Assert.assertEquals(btcAsset, aggregate.asset)
        Assert.assertEquals(Currency.USD, aggregate.currency)
        Assert.assertEquals("Explorer", aggregate.explorer.name)
    }

    @Test
    fun testAmountPlain_withPrice() {
        val transaction = createTransaction(
            type = TransactionType.Transfer,
            value = "100000000",
        )
        val price = Price(
            price = 50000.0,
            priceChangePercentage24h = 0.0,
            updatedAt = System.currentTimeMillis(),
        )
        val extended = createTransactionExtended(transaction, asset = btcAsset, price = price)
        val aggregate = createAggregate(extended)

        val amount = aggregate.amount
        Assert.assertTrue(amount is TransactionDetailsValue.Amount.Plain)
        val plainAmount = amount as TransactionDetailsValue.Amount.Plain
        Assert.assertEquals(btcAsset, plainAmount.asset)
        Assert.assertEquals("1.00 BTC", plainAmount.value)
        Assert.assertEquals("\$50,000.00", plainAmount.equivalent)
    }

    @Test
    fun testAmountPlain_withoutPrice() {
        val transaction = createTransaction(
            type = TransactionType.Transfer,
            value = "100000000",
        )
        val extended = createTransactionExtended(transaction, asset = btcAsset, price = null)
        val aggregate = createAggregate(extended)

        val amount = aggregate.amount
        Assert.assertTrue(amount is TransactionDetailsValue.Amount.Plain)
        val plainAmount = amount as TransactionDetailsValue.Amount.Plain
        Assert.assertEquals(btcAsset, plainAmount.asset)
        Assert.assertEquals("1.00 BTC", plainAmount.value)
        Assert.assertEquals("", plainAmount.equivalent)
    }

    @Test
    fun testAmountSwap_withValidMetadata() {
        val bnbAsset = Asset(
            id = AssetId(Chain.SmartChain),
            name = "BNB",
            symbol = "BNB",
            decimals = 18,
            type = AssetType.NATIVE,
        )
        val tonAsset = Asset(
            id = AssetId(Chain.SmartChain, "0x76A797A59Ba2C17726896976B7B3747BfD1d220f"),
            name = "Ton",
            symbol = "TON",
            decimals = 9,
            type = AssetType.BEP20,
        )

        val swapMetadata = TransactionSwapMetadata(
            fromAsset = bnbAsset.id,
            toAsset = tonAsset.id,
            fromValue = "90",
            toValue = "190",
            provider = SwapProvider.PancakeswapV3.string,
        )
        val metadata = jsonEncoder.encodeToString(TransactionSwapMetadata.serializer(), swapMetadata)

        val transaction = createTransaction(
            type = TransactionType.Swap,
            assetId = bnbAsset.id,
            value = "90000000000000000",
            metadata = metadata,
        )
        val extended = createTransactionExtended(
            transaction = transaction,
            asset = bnbAsset,
            assets = listOf(bnbAsset, tonAsset),
        )
        val associatedAssets = listOf(createAssetInfo(bnbAsset), createAssetInfo(tonAsset))
        val aggregate = createAggregate(extended, associatedAssets)

        val amount = aggregate.amount
        Assert.assertTrue(amount is TransactionDetailsValue.Amount.Swap)
        val swapAmount = amount as TransactionDetailsValue.Amount.Swap
        Assert.assertEquals(bnbAsset, swapAmount.fromAsset)
        Assert.assertEquals(tonAsset, swapAmount.toAsset)
        Assert.assertEquals("90", swapAmount.fromValue)
        Assert.assertEquals("190", swapAmount.toValue)
        Assert.assertEquals(Currency.USD, swapAmount.currency)
    }

    @Test
    fun testAmountSwap_missingMetadata() {
        val transaction = createTransaction(
            type = TransactionType.Swap,
            value = "90000000000000000",
            metadata = null,
        )
        val extended = createTransactionExtended(transaction, asset = ethAsset)
        val aggregate = createAggregate(extended)

        val amount = aggregate.amount
        Assert.assertTrue(amount is TransactionDetailsValue.Amount.None)
    }

    @Test
    fun testAmountSwap_missingAssets() {
        val bnbAsset = Asset(
            id = AssetId(Chain.SmartChain),
            name = "BNB",
            symbol = "BNB",
            decimals = 18,
            type = AssetType.NATIVE,
        )

        val swapMetadata = TransactionSwapMetadata(
            fromAsset = bnbAsset.id,
            toAsset = AssetId(Chain.SmartChain, "0xMISSING"),
            fromValue = "90000000000000000",
            toValue = "19000000000",
        )
        val metadata = jsonEncoder.encodeToString(TransactionSwapMetadata.serializer(), swapMetadata)

        val transaction = createTransaction(
            type = TransactionType.Swap,
            assetId = bnbAsset.id,
            value = "90000000000000000",
            metadata = metadata,
        )
        val extended = createTransactionExtended(transaction, asset = bnbAsset)
        val associatedAssets = listOf(createAssetInfo(bnbAsset))
        val aggregate = createAggregate(extended, associatedAssets)

        val amount = aggregate.amount
        Assert.assertTrue(amount is TransactionDetailsValue.Amount.None)
    }

    @Test
    fun testAmountNFT_withMetadata() {
        val nftAsset = NFTAsset(
            id = "nft-123",
            collectionId = "collection-1",
            contractAddress = "0xcontract",
            tokenId = "123",
            tokenType = NFTType.ERC721,
            name = "NFT Name",
            description = "NFT Description",
            chain = Chain.Ethereum,
            resource = NFTResource(url = "https://example.com/image.png", mimeType = "image/png"),
            images = NFTImages(
                preview = NFTResource(
                    url = "https://example.com/image.png",
                    mimeType = "image/png"
                )
            ),
            attributes = emptyList(),
        )
        val nftMetadata = jsonEncoder.encodeToString(NFTAsset.serializer(), nftAsset)

        val transaction = createTransaction(
            type = TransactionType.TransferNFT,
            value = "1",
            metadata = nftMetadata,
        )
        val extended = createTransactionExtended(transaction, asset = ethAsset)
        val aggregate = createAggregate(extended)

        val amount = aggregate.amount
        Assert.assertTrue(amount is TransactionDetailsValue.Amount.NFT)
        val nftAmount = amount as TransactionDetailsValue.Amount.NFT
        Assert.assertEquals("NFT Name", nftAmount.asset.name)
        Assert.assertEquals("123", nftAmount.asset.tokenId)
    }

    @Test
    fun testAmountNFT_missingMetadata() {
        val transaction = createTransaction(
            type = TransactionType.TransferNFT,
            value = "1",
            metadata = null,
        )
        val extended = createTransactionExtended(transaction, asset = ethAsset)
        val aggregate = createAggregate(extended)

        val amount = aggregate.amount
        Assert.assertTrue(amount is TransactionDetailsValue.Amount.None)
    }

    @Test
    fun testFee_withPrice() {
        val transaction = createTransaction(
            fee = "1000",
        )
        val feePrice = Price(
            price = 50000.0,
            priceChangePercentage24h = 0.0,
            updatedAt = System.currentTimeMillis(),
        )
        val extended = createTransactionExtended(transaction, asset = btcAsset, feePrice = feePrice)
        val aggregate = createAggregate(extended)

        val fee = aggregate.fee
        Assert.assertEquals(btcAsset, fee.asset)
        Assert.assertEquals("0.00001 BTC", fee.value)
        Assert.assertEquals("\$0.50", fee.equivalent)
    }

    @Test
    fun testFee_withoutPrice() {
        val transaction = createTransaction(
            fee = "1000",
        )
        val extended = createTransactionExtended(transaction, asset = btcAsset, feePrice = null)
        val aggregate = createAggregate(extended)

        val fee = aggregate.fee
        Assert.assertEquals(btcAsset, fee.asset)
        Assert.assertEquals("0.00001 BTC", fee.value)
        Assert.assertEquals("", fee.equivalent)
    }

    @Test
    fun testFee_differentAsset() {
        val transaction = createTransaction(
            fee = "1000000000000000",
        )
        val extended = createTransactionExtended(
            transaction,
            asset = usdtAsset,
            feeAsset = ethAsset,
        )
        val aggregate = createAggregate(extended)

        val fee = aggregate.fee
        Assert.assertEquals(ethAsset, fee.asset)
        Assert.assertEquals("0.001 ETH", fee.value)
        Assert.assertEquals("", fee.equivalent)
    }

    @Test
    fun testDate() {
        val transaction = createTransaction()
        val extended = createTransactionExtended(transaction)
        val aggregate = createAggregate(extended)

        val date = aggregate.date
        Assert.assertEquals("January 6, 2026, 2:13 AM", date.data)
    }

    @Test
    fun testStatus() {
        val transaction = createTransaction(state = TransactionState.Pending)
        val extended = createTransactionExtended(transaction)
        val aggregate = createAggregate(extended)

        val status = aggregate.status
        Assert.assertEquals(TransactionState.Pending, status.data)
    }

    @Test
    fun testMemo_present() {
        val transaction = createTransaction(memo = "Test memo")
        val extended = createTransactionExtended(transaction)
        val aggregate = createAggregate(extended)

        val memo = aggregate.memo
        Assert.assertNotNull(memo)
        Assert.assertEquals("Test memo", memo?.data)
    }

    @Test
    fun testMemo_absent() {
        val transaction = createTransaction(memo = null)
        val extended = createTransactionExtended(transaction)
        val aggregate = createAggregate(extended)

        val memo = aggregate.memo
        Assert.assertNull(memo)
    }

    @Test
    fun testMemo_empty() {
        val transaction = createTransaction(memo = "")
        val extended = createTransactionExtended(transaction)
        val aggregate = createAggregate(extended)

        val memo = aggregate.memo
        Assert.assertNull(memo)
    }

    @Test
    fun testNetwork() {
        val transaction = createTransaction()
        val extended = createTransactionExtended(transaction, asset = btcAsset)
        val aggregate = createAggregate(extended)

        val network = aggregate.network
        Assert.assertEquals(btcAsset, network.data)
    }

    @Test
    fun testDestination_transferOutgoing() {
        val transaction = createTransaction(
            type = TransactionType.Transfer,
            direction = TransactionDirection.Outgoing,
            to = "recipient-address",
        )
        val extended = createTransactionExtended(transaction)
        val aggregate = createAggregate(extended)

        val destination = aggregate.destination
        Assert.assertTrue(destination is TransactionDetailsValue.Destination.Recipient)
        val recipient = destination as TransactionDetailsValue.Destination.Recipient
        Assert.assertEquals("recipient-address", recipient.data)
    }

    @Test
    fun testDestination_transferIncoming() {
        val transaction = createTransaction(
            type = TransactionType.Transfer,
            direction = TransactionDirection.Incoming,
            from = "sender-address",
        )
        val extended = createTransactionExtended(transaction)
        val aggregate = createAggregate(extended)

        val destination = aggregate.destination
        Assert.assertTrue(destination is TransactionDetailsValue.Destination.Sender)
        val sender = destination as TransactionDetailsValue.Destination.Sender
        Assert.assertEquals("sender-address", sender.data)
    }

    @Test
    fun testDestination_transferSelfTransfer() {
        val transaction = createTransaction(
            type = TransactionType.Transfer,
            direction = TransactionDirection.SelfTransfer,
            to = "self-address",
        )
        val extended = createTransactionExtended(transaction)
        val aggregate = createAggregate(extended)

        val destination = aggregate.destination
        Assert.assertTrue(destination is TransactionDetailsValue.Destination.Recipient)
        val recipient = destination as TransactionDetailsValue.Destination.Recipient
        Assert.assertEquals("self-address", recipient.data)
    }

    @Test
    fun testDestination_swapWithProvider() {
        val swapMetadata = TransactionSwapMetadata(
            fromAsset = ethAsset.id,
            toAsset = usdtAsset.id,
            fromValue = "1000000000000000000",
            toValue = "1500000000",
            provider = SwapProvider.UniswapV3.string,
        )
        val metadata = jsonEncoder.encodeToString(TransactionSwapMetadata.serializer(), swapMetadata)

        val transaction = createTransaction(
            type = TransactionType.Swap,
            metadata = metadata,
        )
        val extended = createTransactionExtended(transaction, asset = ethAsset)
        val associatedAssets = listOf(createAssetInfo(ethAsset), createAssetInfo(usdtAsset))
        val aggregate = createAggregate(extended, associatedAssets)

        val destination = aggregate.destination
        Assert.assertTrue(destination is TransactionDetailsValue.Destination.Provider)
        val provider = destination as TransactionDetailsValue.Destination.Provider
        Assert.assertEquals(SwapProvider.UniswapV3, provider.data)
    }

    @Test
    fun testDestination_stakeDelegate() {
        val transaction = createTransaction(type = TransactionType.StakeDelegate)
        val extended = createTransactionExtended(transaction)
        val aggregate = createAggregate(extended)

        val destination = aggregate.destination
        Assert.assertNull(destination)
    }

    @Test
    fun testDestination_tokenApproval() {
        val transaction = createTransaction(type = TransactionType.TokenApproval)
        val extended = createTransactionExtended(transaction)
        val aggregate = createAggregate(extended)

        val destination = aggregate.destination
        Assert.assertNull(destination)
    }

    @Test
    fun testValueGroups() {
        val transaction = createTransaction(memo = "Test memo")
        val extended = createTransactionExtended(transaction)
        val aggregate = createAggregate(extended)

        val valueGroups = aggregate.valueGroups
        Assert.assertEquals(4, valueGroups.size)
    }

    @Test
    fun testValueGroups_differentCurrency() {
        val transaction = createTransaction()
        val price = Price(
            price = 50000.0,
            priceChangePercentage24h = 0.0,
            updatedAt = System.currentTimeMillis(),
        )
        val extended = createTransactionExtended(transaction, asset = btcAsset, price = price)
        val aggregate = createAggregate(extended, currency = Currency.EUR)

        Assert.assertEquals(Currency.EUR, aggregate.currency)
        val valueGroups = aggregate.valueGroups
        Assert.assertEquals(4, valueGroups.size)
    }
}