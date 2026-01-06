package com.gemwallet.android.data.coordinates.transaction

import com.gemwallet.android.domains.transaction.aggregates.TransactionDataAggregate
import com.gemwallet.android.model.Transaction
import com.gemwallet.android.model.TransactionExtended
import com.gemwallet.android.serializer.jsonEncoder
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionSwapMetadata
import com.wallet.core.primitives.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TransactionDataAggregateImplTest {

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
        metadata: String? = null,
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
        fee = "1000",
        feeAssetId = assetId,
        value = value,
        memo = null,
        direction = direction,
        utxoInputs = null,
        utxoOutputs = null,
        metadata = metadata,
        createdAt = System.currentTimeMillis(),
    )

    private fun createTransactionExtended(
        transaction: Transaction,
        asset: Asset = btcAsset,
        assets: List<Asset> = emptyList(),
    ) = TransactionExtended(
        transaction = transaction,
        asset = asset,
        feeAsset = asset,
        price = null,
        feePrice = null,
        assets = assets,
    )

    private fun createAggregate(transaction: TransactionExtended): TransactionDataAggregate =
        TransactionDataAggregateImpl(transaction)

    @Test
    fun testBasicPropertyDelegation() {
        val transaction = createTransaction(
            id = "test-id-123",
            state = TransactionState.Pending,
            type = TransactionType.Transfer,
            direction = TransactionDirection.Incoming,
        )
        val extended = createTransactionExtended(transaction)
        val aggregate = createAggregate(extended)

        assertEquals("test-id-123", aggregate.id)
        assertEquals(btcAsset, aggregate.asset)
        assertEquals(TransactionType.Transfer, aggregate.type)
        assertEquals(TransactionDirection.Incoming, aggregate.direction)
        assertEquals(TransactionState.Pending, aggregate.state)
        assertEquals(transaction.createdAt, aggregate.createdAt)
    }

    @Test
    fun testIsPending_whenStatePending() {
        val transaction = createTransaction(state = TransactionState.Pending)
        val extended = createTransactionExtended(transaction)
        val aggregate = createAggregate(extended)

        assertEquals(true, aggregate.isPending)
    }

    @Test
    fun testIsPending_whenStateConfirmed() {
        val transaction = createTransaction(state = TransactionState.Confirmed)
        val extended = createTransactionExtended(transaction)
        val aggregate = createAggregate(extended)

        assertEquals(false, aggregate.isPending)
    }

    @Test
    fun testAddress_transferOutgoing() {
        val transaction = createTransaction(
            type = TransactionType.Transfer,
            direction = TransactionDirection.Outgoing,
            from = "bc1qsender",
            to = "bc1qreceiver",
        )
        val extended = createTransactionExtended(transaction)
        val aggregate = createAggregate(extended)

        assertEquals("bc1q....iver", aggregate.address)
    }

    @Test
    fun testAddress_transferIncoming() {
        val transaction = createTransaction(
            type = TransactionType.Transfer,
            direction = TransactionDirection.Incoming,
            from = "bc1qsender",
            to = "bc1qreceiver",
        )
        val extended = createTransactionExtended(transaction)
        val aggregate = createAggregate(extended)

        assertEquals("bc1qsender", aggregate.address)
    }

    @Test
    fun testAddress_transferSelfTransfer() {
        val transaction = createTransaction(
            type = TransactionType.Transfer,
            direction = TransactionDirection.SelfTransfer,
            from = "bc1qsender",
            to = "bc1qsender",
        )
        val extended = createTransactionExtended(transaction)
        val aggregate = createAggregate(extended)

        assertEquals("bc1qsender", aggregate.address)
    }

    @Test
    fun testAddress_swapTransaction() {
        val transaction = createTransaction(
            type = TransactionType.Swap,
            direction = TransactionDirection.Outgoing,
        )
        val extended = createTransactionExtended(transaction)
        val aggregate = createAggregate(extended)

        assertEquals("", aggregate.address)
    }

    @Test
    fun testAddress_stakeDelegate() {
        val transaction = createTransaction(
            type = TransactionType.StakeDelegate,
            direction = TransactionDirection.Outgoing,
        )
        val extended = createTransactionExtended(transaction)
        val aggregate = createAggregate(extended)

        assertEquals("", aggregate.address)
    }

    @Test
    fun testValue_transferOutgoing() {
        val transaction = createTransaction(
            type = TransactionType.Transfer,
            direction = TransactionDirection.Outgoing,
            value = "100000000",
        )
        val extended = createTransactionExtended(transaction, asset = btcAsset)
        val aggregate = createAggregate(extended)

        assertEquals("-1.00 BTC", aggregate.value)
        assertNull(aggregate.equivalentValue)
    }

    @Test
    fun testValue_transferIncoming() {
        val transaction = createTransaction(
            type = TransactionType.Transfer,
            direction = TransactionDirection.Incoming,
            value = "50000000",
        )
        val extended = createTransactionExtended(transaction, asset = btcAsset)
        val aggregate = createAggregate(extended)

        assertEquals("+0.50 BTC", aggregate.value)
        assertNull(aggregate.equivalentValue)
    }

    @Test
    fun testValue_transferSelfTransfer() {
        val transaction = createTransaction(
            type = TransactionType.Transfer,
            direction = TransactionDirection.SelfTransfer,
            value = "25000000",
        )
        val extended = createTransactionExtended(transaction, asset = btcAsset)
        val aggregate = createAggregate(extended)

        assertEquals("-0.25 BTC", aggregate.value)
        assertNull(aggregate.equivalentValue)
    }

    @Test
    fun testValue_stakeDelegate() {
        val transaction = createTransaction(
            type = TransactionType.StakeDelegate,
            direction = TransactionDirection.Outgoing,
            value = "1000000000000000000",
        )
        val extended = createTransactionExtended(transaction, asset = ethAsset)
        val aggregate = createAggregate(extended)

        assertEquals("1.00 ETH", aggregate.value)
        assertNull(aggregate.equivalentValue)
    }

    @Test
    fun testValue_stakeUndelegate() {
        val transaction = createTransaction(
            type = TransactionType.StakeUndelegate,
            direction = TransactionDirection.Incoming,
            value = "2000000000000000000",
        )
        val extended = createTransactionExtended(transaction, asset = ethAsset)
        val aggregate = createAggregate(extended)

        assertEquals("2.00 ETH", aggregate.value)
        assertNull(aggregate.equivalentValue)
    }

    @Test
    fun testValue_stakeRewards() {
        val transaction = createTransaction(
            type = TransactionType.StakeRewards,
            direction = TransactionDirection.Incoming,
            value = "500000000000000000",
        )
        val extended = createTransactionExtended(transaction, asset = ethAsset)
        val aggregate = createAggregate(extended)

        assertEquals("0.50 ETH", aggregate.value)
        assertNull(aggregate.equivalentValue)
    }

    @Test
    fun testValue_tokenApproval() {
        val transaction = createTransaction(
            type = TransactionType.TokenApproval,
            direction = TransactionDirection.Outgoing,
            value = "1000000",
        )
        val extended = createTransactionExtended(transaction, asset = usdtAsset)
        val aggregate = createAggregate(extended)

        assertEquals("", aggregate.value)
        assertNull(aggregate.equivalentValue)
    }

    @Test
    fun testValue_smartContractCall() {
        val transaction = createTransaction(
            type = TransactionType.SmartContractCall,
            direction = TransactionDirection.Outgoing,
            value = "1000000000000000000",
        )
        val extended = createTransactionExtended(transaction, asset = ethAsset)
        val aggregate = createAggregate(extended)

        assertEquals("", aggregate.value)
        assertNull(aggregate.equivalentValue)
    }

    @Test
    fun testValue_swap() {
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
            fromValue = "90000000000000000",
            toValue = "19000000000",
        )
        val metadata = jsonEncoder.encodeToString(TransactionSwapMetadata.serializer(), swapMetadata)

        val transaction = createTransaction(
            type = TransactionType.Swap,
            direction = TransactionDirection.Outgoing,
            assetId = bnbAsset.id,
            value = "90000000000000000",
            metadata = metadata,
        )
        val extended = createTransactionExtended(
            transaction = transaction,
            asset = bnbAsset,
            assets = listOf(bnbAsset, tonAsset),
        )
        val aggregate = createAggregate(extended)

        assertEquals(aggregate.value,"+19.00 TON")
        assertEquals(aggregate.equivalentValue, "-0.09 BNB")
    }

    @Test
    fun testValue_swapMissingMetadata() {
        val transaction = createTransaction(
            type = TransactionType.Swap,
            direction = TransactionDirection.Outgoing,
            value = "90000000000000000",
            metadata = null,
        )
        val extended = createTransactionExtended(transaction, asset = ethAsset)
        val aggregate = createAggregate(extended)

        assertEquals("", aggregate.value)
        assertNull(aggregate.equivalentValue)
    }

    @Test
    fun testValue_smallAmount() {
        val transaction = createTransaction(
            type = TransactionType.Transfer,
            direction = TransactionDirection.Outgoing,
            value = "1345",
        )
        val extended = createTransactionExtended(transaction, asset = btcAsset)
        val aggregate = createAggregate(extended)

        assertEquals("-0.00001345 BTC", aggregate.value)
    }

    @Test
    fun testValue_largeAmount() {
        val transaction = createTransaction(
            type = TransactionType.Transfer,
            direction = TransactionDirection.Incoming,
            value = "2100000000000000",
        )
        val extended = createTransactionExtended(transaction, asset = btcAsset)
        val aggregate = createAggregate(extended)

        assertEquals("+21,000,000.00 BTC", aggregate.value)
    }
}