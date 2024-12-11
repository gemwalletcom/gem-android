package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.ext.asset
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ConfirmParams
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.DelegationBase
import com.wallet.core.primitives.DelegationState
import com.wallet.core.primitives.DelegationValidator
import uniffi.gemstone.BscDelegation
import uniffi.gemstone.BscDelegationStatus

object StakeHub {
    val offset: UShort = (0).toUShort()
    const val address = "0x0000000000000000000000000000000000002002"
    const val reader = "0x830295c0abe7358f7e24bc38408095621474280b"

    fun encodeMaxElectedValidators(): String {
        // cast calldata "maxElectedValidators()"
        return "0xc473318f"
    }

    fun encodeStake(params: ConfirmParams): String {
        return when (params) {
            is ConfirmParams.Stake.DelegateParams -> encodeDelegateCall(
                params.validatorId,
                false
            )

            is ConfirmParams.Stake.RedelegateParams -> encodeRedelegateCall(params, false)
            is ConfirmParams.Stake.UndelegateParams -> encodeUndelegateCall(params)
            is ConfirmParams.Stake.WithdrawParams -> encodeClaim(params, (0).toULong())
            else -> throw IllegalArgumentException()
        }.toHexString()
    }

    fun encodeDelegateCall(validator: String, delegateVote: Boolean): ByteArray {
        return uniffi.gemstone.bscEncodeDelegateCall(validator, delegateVote)
    }

    fun encodeValidatorsCall(offset: Int, limit: Int): String {
        return uniffi.gemstone.bscEncodeValidatorsCall(offset.toUShort(), limit.toUShort())
            .toHexString()
    }

    fun decodeValidatorsReturn(hexData: String): List<DelegationValidator> {
        return uniffi.gemstone.bscDecodeValidatorsReturn(hexData.decodeHex()).map {
            DelegationValidator(
                chain = Chain.SmartChain,
                id = it.operatorAddress,
                name = it.moniker,
                isActive = !it.jailed,
                commision = it.commission.toDouble() / 100,
                apr = it.apy.toDouble() / 100,
            )
        }
    }

    fun encodeDelegationsCall(address: String, limit: Int): String {
        return uniffi.gemstone.bscEncodeDelegationsCall(address, offset, limit.toUShort())
            .toHexString()
    }

    fun decodeDelegationsResult(data: String): List<DelegationBase> {
        return uniffi.gemstone.bscDecodeDelegationsReturn(data.decodeHex()).map { it.into() }
    }

    fun encodeUndelegationsCall(address: String, limit: Int): String {
        return uniffi.gemstone.bscEncodeUndelegationsCall(address, offset, limit.toUShort())
            .toHexString()
    }

    fun decodeUnelegationsResult(data: String): List<DelegationBase> {
        return uniffi.gemstone.bscDecodeUndelegationsReturn(data.decodeHex()).map { it.into() }
    }

    fun encodeUndelegateCall(params: ConfirmParams.Stake.UndelegateParams): ByteArray {
        val amountShare =
            params.amount * params.share!!.toBigInteger() / params.balance!!.toBigInteger()
        return uniffi.gemstone.bscEncodeUndelegateCall(
            operatorAddress = params.validatorId,
            shares = amountShare.toString()
        )
    }

    fun encodeRedelegateCall(
        params: ConfirmParams.Stake.RedelegateParams,
        votePower: Boolean
    ): ByteArray {
        val amountShare =
            params.amount * params.share!!.toBigInteger() / params.balance!!.toBigInteger()
        return uniffi.gemstone.bscEncodeRedelegateCall(
            srcValidator = params.srcValidatorId,
            dstValidator = params.dstValidatorId,
            shares = amountShare.toString(),
            delegateVotePower = votePower,
        )
    }

    fun encodeClaim(
        params: ConfirmParams.Stake.WithdrawParams,
        requestNumber: ULong
    ): ByteArray {
        return uniffi.gemstone.bscEncodeClaimCall(
            operatorAddress = params.validatorId,
            requestNumber = requestNumber
        )
    }
}

fun BscDelegation.into(): DelegationBase {
    val completion = when (status) {
        BscDelegationStatus.ACTIVE -> null
        BscDelegationStatus.UNDELEGATING -> unlockTime?.toLong()
    }?.toLong()
    val state = when (status) {
        BscDelegationStatus.ACTIVE -> DelegationState.Active
        BscDelegationStatus.UNDELEGATING -> {
            if (completion == null) {
                DelegationState.Undelegating
            } else  if (System.currentTimeMillis() <= completion * 1000) {
                DelegationState.Undelegating
            } else {
                DelegationState.AwaitingWithdrawal
            }
        }
    }
    val delegationId = when (status) {
        BscDelegationStatus.ACTIVE -> ""
        BscDelegationStatus.UNDELEGATING -> "$validatorAddress--${unlockTime ?: 0}"
    }
    return DelegationBase(
        assetId = Chain.SmartChain.asset().id,
        state = state,
        balance = amount,
        shares = shares,
        rewards = "0",
        completionDate = completion,
        delegationId = delegationId,
        validatorId = validatorAddress,
    )
}