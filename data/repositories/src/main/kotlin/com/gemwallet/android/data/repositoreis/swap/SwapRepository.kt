package com.gemwallet.android.data.repositoreis.swap

import com.gemwallet.android.blockchain.operators.LoadPrivateKeyOperator
import com.gemwallet.android.blockchain.operators.PasswordStore
import com.gemwallet.android.blockchain.services.SignClientProxy
import com.gemwallet.android.cases.swap.GetSwapQuotes
import com.gemwallet.android.cases.swap.GetSwapSupported
import com.gemwallet.android.domains.asset.chain
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Wallet
import uniffi.gemstone.Config
import uniffi.gemstone.FetchQuoteData
import uniffi.gemstone.GemSwapper
import uniffi.gemstone.Permit2Data
import uniffi.gemstone.Permit2Detail
import uniffi.gemstone.PermitSingle
import uniffi.gemstone.SwapperAssetList
import uniffi.gemstone.SwapperMode
import uniffi.gemstone.SwapperOptions
import uniffi.gemstone.SwapperQuote
import uniffi.gemstone.SwapperQuoteAsset
import uniffi.gemstone.SwapperQuoteData
import uniffi.gemstone.SwapperQuoteRequest
import uniffi.gemstone.getDefaultSlippage
import uniffi.gemstone.permit2DataToEip712Json
import java.math.BigInteger

class SwapRepository(
    private val gemSwapper: GemSwapper,
    private val signClient: SignClientProxy,
    private val passwordStore: PasswordStore,
    private val loadPrivateKeyOperator: LoadPrivateKeyOperator,
) : GetSwapSupported, GetSwapQuotes {

    override suspend fun getQuotes(ownerAddress: String, destination: String, from: Asset, to: Asset, amount: String): List<SwapperQuote>? {
        val swapRequest = SwapperQuoteRequest(
            fromAsset = SwapperQuoteAsset(
                id = from.id.toIdentifier(),
                symbol = from.symbol,
                decimals = from.decimals.toUInt(),
            ),
            toAsset = SwapperQuoteAsset(
                id = to.id.toIdentifier(),
                symbol = to.symbol,
                decimals = to.decimals.toUInt(),
            ),
            walletAddress = ownerAddress,
            destinationAddress = destination,
            value = amount,
            mode = SwapperMode.EXACT_IN,
            options = SwapperOptions(
                slippage = getDefaultSlippage(from.chain.string),
                fee = Config().getSwapConfig().referralFee,
                preferredProviders = emptyList(),
            )
        )
        val quote = gemSwapper.fetchQuote(swapRequest)
            .sortedByDescending { BigInteger(it.toValue) }
        return quote
    }

    suspend fun getQuoteData(quote: SwapperQuote, wallet: Wallet): SwapperQuoteData {
        val permit = gemSwapper.fetchPermit2ForQuote(quote = quote)

        if (permit == null) {
            return gemSwapper.fetchQuoteData(quote, FetchQuoteData.None)
        }

        val permit2Single = permit2Single(
            token = permit.token,
            spender = permit.spender,
            value = permit.value,
            nonce = permit.permit2Nonce
        )
        val chain = quote.request.fromAsset.id.toAssetId()?.chain ?: throw Exception()
        val permit2Json = permit2DataToEip712Json(
            chain = chain.string,
            data = permit2Single,
            contract = permit.permit2Contract
        )
        val signature = signClient.signTypedMessage(
            chain = chain,
            input = permit2Json.toByteArray(),
            privateKey = loadPrivateKeyOperator.invoke(wallet, chain, passwordStore.getPassword(walletId = wallet.id)),
        )
        val permitData = Permit2Data(permit2Single, signature)
        return gemSwapper.fetchQuoteData(quote, FetchQuoteData.Permit2(permitData))
    }

    private fun permit2Single(token: String, spender: String, value: String, nonce: ULong): PermitSingle {
        return PermitSingle(
            details = Permit2Detail(
                token = token,
                amount = value,
                expiration = (System.currentTimeMillis() / 1000 + 60 * 60 * 30).toULong(),
                nonce = nonce
            ),
            spender = spender,
            sigDeadline = (System.currentTimeMillis() / 1000 + 60 * 30).toULong(),
        )
    }

    override fun getSwapSupportChains(assetId: AssetId): SwapperAssetList {
        return gemSwapper.supportedChainsForFromAsset(assetId.toIdentifier())
    }
}