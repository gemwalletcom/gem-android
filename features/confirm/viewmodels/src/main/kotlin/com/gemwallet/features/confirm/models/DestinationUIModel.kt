package com.gemwallet.features.confirm.models

import com.gemwallet.android.model.ConfirmParams
import com.wallet.core.primitives.DelegationValidator

sealed class DestinationUIModel(val data: String) {
    class Stake(data: String) : DestinationUIModel(data)

    class Provider(data: String) : DestinationUIModel(data)

    class Transfer(val domain: String?, val address: String) : DestinationUIModel(address)

    companion object {
        fun map(params: ConfirmParams, validator: DelegationValidator?): DestinationUIModel? {
            return when (params) {
                is ConfirmParams.Activate,
                is ConfirmParams.Stake.RewardsParams -> null
                is ConfirmParams.Stake.DelegateParams,
                is ConfirmParams.Stake.RedelegateParams,
                is ConfirmParams.Stake.UndelegateParams,
                is ConfirmParams.Stake.WithdrawParams -> Stake(data = validator?.name ?: "")
                is ConfirmParams.SwapParams -> Provider(data = params.provider)// TODO: val swapProvider = SwapProvider.entries.firstOrNull { it.string == protocolId }
                is ConfirmParams.TokenApprovalParams -> Provider(data = params.provider)
                is ConfirmParams.NftParams,
                is ConfirmParams.TransferParams -> {
                    return params.destination()?.let {
                        Transfer(domain = it.domainName, address = it.address)
                    } ?: throw ConfirmError.RecipientEmpty
                }
            }
        }
    }
}