package com.gemwallet.android.data.repositoreis.swap

import com.gemwallet.android.blockchain.clients.SignClientProxy
import com.gemwallet.android.blockchain.operators.LoadPrivateKeyOperator
import com.gemwallet.android.blockchain.operators.PasswordStore
import com.gemwallet.android.cases.nodes.GetCurrentNodeCase
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Wallet
import uniffi.gemstone.ApprovalType
import uniffi.gemstone.FetchQuoteData
import uniffi.gemstone.GemSwapFee
import uniffi.gemstone.GemSwapMode
import uniffi.gemstone.GemSwapOptions
import uniffi.gemstone.GemSwapper
import uniffi.gemstone.Permit2Data
import uniffi.gemstone.Permit2Detail
import uniffi.gemstone.PermitSingle
import uniffi.gemstone.SwapQuote
import uniffi.gemstone.SwapQuoteData
import uniffi.gemstone.permit2DataToEip712Json

class SwapRepository(
    private val signClient: SignClientProxy,
    private val passwordStore: PasswordStore,
    private val loadPrivateKeyOperator: LoadPrivateKeyOperator,
    getCurrentNodeCase: GetCurrentNodeCase,
) {
    private val gemSwapper = GemSwapper(NativeProvider(getCurrentNodeCase))

    suspend fun getQuote(ownerAddress: String, from: AssetId, to: AssetId, amount: String): SwapQuote? {
        val swapRequest = uniffi.gemstone.SwapQuoteRequest(
            fromAsset = from.toIdentifier(),
            toAsset = to.toIdentifier(),
            walletAddress = ownerAddress,
            destinationAddress = ownerAddress,
            value = amount,
            mode = GemSwapMode.EXACT_IN,
            options = GemSwapOptions(
                slippageBps = 100u,
                fee = GemSwapFee(bps = 50u, address = "0x0D9DAB1A248f63B0a48965bA8435e4de7497a3dC"),
                preferredProviders = emptyList()
            )
        )
        val quote = gemSwapper.fetchQuote(swapRequest).firstOrNull() ?: return null
        return quote
    }

    suspend fun getQuoteData(quote: SwapQuote, wallet: Wallet): SwapQuoteData {
        val permit2Data = when (quote.approval) {
            ApprovalType.None, is ApprovalType.Approve -> FetchQuoteData.None
            is ApprovalType.Permit2 -> FetchQuoteData.Permit2(createPermit2(quote, wallet))
        }
        return gemSwapper.fetchQuoteData(quote, permit2Data)
    }

    private suspend fun createPermit2(quote: SwapQuote, wallet: Wallet): Permit2Data {
        val approval = quote.approval as ApprovalType.Permit2
        val chain = quote.request.fromAsset.toAssetId()?.chain ?: throw IllegalArgumentException()
        val permit2Single = permit2Single(approval.v1.token, approval.v1.spender, approval.v1.value, approval.v1.permit2Nonce)
        val permit2Json = permit2DataToEip712Json(chain.string, permit2Single)
        val signature = signClient.signTypedMessage(
            chain = chain,
            input = permit2Json.toByteArray(),
            privateKey = loadPrivateKeyOperator.invoke(wallet, chain, passwordStore.getPassword(walletId = wallet.id)),
        )
        return Permit2Data(permit2Single, signature)
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
}