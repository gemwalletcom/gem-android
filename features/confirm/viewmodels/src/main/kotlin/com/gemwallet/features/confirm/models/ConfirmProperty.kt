package com.gemwallet.features.confirm.models

import com.gemwallet.android.model.ConfirmParams
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.DelegationValidator

sealed interface ConfirmProperty {
    class Source(val data: String) : ConfirmProperty

    class Network(val data: Asset) : ConfirmProperty

    class Memo(val data: String) : ConfirmProperty

    sealed class Destination(val data: String) : ConfirmProperty {
        class Stake(data: String) : Destination(data)

        class Provider(data: String) : Destination(data)

        class Transfer(val domain: String?, val address: String) : Destination(address)

        class App(val name: String) : Destination(name)

        companion object {
            fun map(params: ConfirmParams, validator: DelegationValidator?): Destination? {
                return when (params) {
                    is ConfirmParams.Activate,
                    is ConfirmParams.Stake.Freeze,
                    is ConfirmParams.Stake.Unfreeze,
                    is ConfirmParams.Stake.RewardsParams -> null
                    is ConfirmParams.Stake.DelegateParams,
                    is ConfirmParams.Stake.RedelegateParams,
                    is ConfirmParams.Stake.UndelegateParams,
                    is ConfirmParams.Stake.WithdrawParams -> Stake(data = validator?.name ?: "")
                    is ConfirmParams.SwapParams -> Provider(data = params.providerName)
                    is ConfirmParams.TokenApprovalParams -> Provider(data = params.provider)
                    is ConfirmParams.NftParams,
                    is ConfirmParams.TransferParams.Token,
                    is ConfirmParams.TransferParams.Native -> {
                        return params.destination()?.let {
                            Transfer(domain = it.name, address = it.address)

                        } ?: throw ConfirmError.RecipientEmpty
                    }
                    is ConfirmParams.TransferParams.Generic -> App(params.name)
                }
            }
        }
    }
}