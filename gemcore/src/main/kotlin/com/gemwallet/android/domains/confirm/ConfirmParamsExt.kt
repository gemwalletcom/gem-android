package com.gemwallet.android.domains.confirm

import com.gemwallet.android.model.ConfirmParams
import uniffi.gemstone.GemApprovalData
import uniffi.gemstone.GemSwapData
import uniffi.gemstone.GemSwapProviderData
import uniffi.gemstone.GemSwapQuote
import uniffi.gemstone.GemSwapQuoteData

fun ConfirmParams.SwapParams.toGem(): GemSwapData {
    return GemSwapData(
        quote = GemSwapQuote(
            fromAddress = from.address,
            toAddress = toAddress,
            fromValue = fromAmount.toString(),
            toValue = toAmount.toString(),
            providerData = GemSwapProviderData(
                provider = providerId,
                protocolName = protocolId,
                name = providerName,
            ),
            slippageBps = slippageBps,
            etaInSeconds = etaInSeconds,
            useMaxAmount = useMaxAmount
        ),
        data = GemSwapQuoteData(
            to = toAddress,
            data = swapData,
            approval = approval?.let {
                GemApprovalData(
                    token = it.token,
                    spender = it.spender,
                    value = it.value
                )
            },
            value = value,
            gasLimit = gasLimit?.toString(),
            dataType = dataType,
            memo = memo()
        )
    )
}