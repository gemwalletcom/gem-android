package com.gemwallet.features.bridge.viewmodels.model

import com.gemwallet.android.blockchain.services.SignClientProxy
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.getShortUrl
import com.gemwallet.android.math.hexToBigInteger
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.ConfirmParams.TransferParams.Generic
import com.gemwallet.android.model.DestinationAddress
import com.reown.walletkit.client.Wallet
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import uniffi.gemstone.MessageSigner
import uniffi.gemstone.TransferDataOutputType
import uniffi.gemstone.WalletConnect
import uniffi.gemstone.WalletConnectAction
import uniffi.gemstone.WalletConnectResponseType
import uniffi.gemstone.WalletConnectTransaction
import uniffi.gemstone.WalletConnectTransactionType
import uniffi.gemstone.WalletConnectionVerificationStatus
import java.math.BigInteger

sealed class WCRequest(
    internal val sessionRequest: Wallet.Model.SessionRequest,
    internal val account: Account,
    val verificationStatus: WalletConnectionVerificationStatus,
) {
    internal val walletConnect = WalletConnect()

    val requestId: Long get() = sessionRequest.request.id

    val topic: String get() = sessionRequest.topic

    val name: String get() = sessionRequest.peerMetaData?.name ?: ""

    val icon: String get() = sessionRequest.peerMetaData?.icons?.firstOrNull() ?: ""
    val description: String get() = sessionRequest.peerMetaData?.description ?: ""
    val uri: String get() = sessionRequest.peerMetaData?.url?.getShortUrl() ?: ""
    val method: String get() = sessionRequest.request.method

    val chain: Chain get() = account.chain

    class SignMessage(
        sessionRequest: Wallet.Model.SessionRequest,
        account: Account,
        verificationStatus: WalletConnectionVerificationStatus,
        val action: WalletConnectAction.SignMessage,
    ) : WCRequest(sessionRequest, account, verificationStatus) {

        val signer: MessageSigner
            get() = MessageSigner(walletConnect.decodeSignMessage(action.chain, action.signType, action.data))

        suspend fun execute(
            signClient: SignClientProxy,
            privateKey: ByteArray
        ): String {
            val signature = signer.sign(privateKey)
            return walletConnect.encodeSignMessage(chain.string, signature).let { encoded ->
                when (encoded) {
                    is WalletConnectResponseType.Object -> encoded.json
                    is WalletConnectResponseType.String -> encoded.value
                }
            }
        }
    }

    abstract class Transaction(
        data: Wallet.Model.SessionRequest,
        account: Account,
        verificationStatus: WalletConnectionVerificationStatus,
    ) : WCRequest(data, account, verificationStatus) {

        abstract val inputType: ConfirmParams.TransferParams.InputType

        abstract val transactionType: WalletConnectTransactionType

        abstract val data: String

        abstract val confirmParams: Generic

        class SignTransaction(
            sessionRequest: Wallet.Model.SessionRequest,
            account: Account,
            verificationStatus: WalletConnectionVerificationStatus,
            val action: WalletConnectAction.SignTransaction,
        ) : Transaction(sessionRequest, account, verificationStatus) {

            override val inputType: ConfirmParams.TransferParams.InputType
                get() = ConfirmParams.TransferParams.InputType.Signature

            override val transactionType: WalletConnectTransactionType
                get() = action.transactionType

            override val data: String
                get() = action.data

            override val confirmParams: Generic
                get() {
                    return walletConnect.decodeSendTransaction(transactionType, data).map(this, false)
                }

            fun execute(signature: String): String {
                return WalletConnect().encodeSignTransaction(action.chain, signature).let {
                    when (it) {
                        is WalletConnectResponseType.Object -> it.json
                        is WalletConnectResponseType.String -> it.value
                    }
                }
            }
        }

        class SendTransaction(
            sessionRequest: Wallet.Model.SessionRequest,
            account: Account,
            verificationStatus: WalletConnectionVerificationStatus,
            val action: WalletConnectAction.SendTransaction,
        ) : Transaction(sessionRequest, account, verificationStatus) {

            override val inputType: ConfirmParams.TransferParams.InputType
                get() = ConfirmParams.TransferParams.InputType.EncodeTransaction

            override val transactionType: WalletConnectTransactionType
                get() = action.transactionType

            override val data: String
                get() = action.data

            override val confirmParams: Generic
                get() {
                    return walletConnect.decodeSendTransaction(transactionType, data).map(this, true)
                }

            fun execute(hash: String): String {
                return walletConnect.encodeSendTransaction(action.chain, hash).let {
                    when (it) {
                        is WalletConnectResponseType.Object -> it.json
                        is WalletConnectResponseType.String -> it.value
                    }
                }
            }
        }
    }
}

private fun WalletConnectTransaction.map(
    request: WCRequest.Transaction,
    isSendable: Boolean,
): Generic {
    val asset = request.chain.asset()
    return when (this) {
        is WalletConnectTransaction.Ethereum -> Generic(
            requestId = request.requestId.toString(),
            asset = asset,
            from = request.account,
            memo = data.data,
            name = request.name,
            description = request.description,
            url = request.uri,
            icon = request.icon,
            gasLimit = data.gasLimit,
            inputType = request.inputType,
            destination = DestinationAddress(data.to),
            amount = data.value?.hexToBigInteger() ?: BigInteger.ZERO,
            isSendable = isSendable,
        )
        is WalletConnectTransaction.Solana -> Generic(
            requestId = request.requestId.toString(),
            asset = asset,
            from = request.account,
            memo = data.transaction,
            name = request.name,
            description = request.description,
            url = request.uri,
            icon = request.icon,
            gasLimit = "",
            inputType = when (outputType) {
                TransferDataOutputType.ENCODED_TRANSACTION -> ConfirmParams.TransferParams.InputType.EncodeTransaction
                TransferDataOutputType.SIGNATURE -> ConfirmParams.TransferParams.InputType.Signature
            },
            destination = DestinationAddress(""),
            amount = BigInteger.ZERO,
            isSendable = isSendable,
        )
        is WalletConnectTransaction.Sui -> Generic(
            requestId = request.requestId.toString(),
            asset = asset,
            from = request.account,
            memo = data.transaction,
            name = request.name,
            description = request.description,
            url = request.uri,
            icon = request.icon,
            gasLimit = "",
            inputType = when (outputType) {
                TransferDataOutputType.ENCODED_TRANSACTION -> ConfirmParams.TransferParams.InputType.EncodeTransaction
                TransferDataOutputType.SIGNATURE -> ConfirmParams.TransferParams.InputType.Signature
            },
            destination = DestinationAddress(""),
            amount = BigInteger.ZERO,
            isSendable = isSendable,
        )
        is WalletConnectTransaction.Bitcoin -> Generic(
            requestId = request.requestId.toString(),
            asset = asset,
            from = request.account,
            name = request.name,
            description = request.description,
            url = request.uri,
            icon = request.icon,
            gasLimit = "",
            inputType = when (outputType) {
                TransferDataOutputType.ENCODED_TRANSACTION -> ConfirmParams.TransferParams.InputType.EncodeTransaction
                TransferDataOutputType.SIGNATURE -> ConfirmParams.TransferParams.InputType.Signature
            },
            destination = DestinationAddress(""),
            amount = BigInteger.ZERO,
            isSendable = isSendable,
        )
        is WalletConnectTransaction.Ton -> Generic(
            requestId = request.requestId.toString(),
            asset = asset,
            from = request.account,
            name = request.name,
            description = request.description,
            url = request.uri,
            icon = request.icon,
            gasLimit = "",
            inputType = when (outputType) {
                TransferDataOutputType.ENCODED_TRANSACTION -> ConfirmParams.TransferParams.InputType.EncodeTransaction
                TransferDataOutputType.SIGNATURE -> ConfirmParams.TransferParams.InputType.Signature
            },
            destination = DestinationAddress(""),
            amount = BigInteger.ZERO,
            isSendable = isSendable,
        )

        is WalletConnectTransaction.Tron -> Generic(
            requestId = request.requestId.toString(),
            asset = asset,
            memo = data,
            from = request.account,
            name = request.name,
            description = request.description,
            url = request.uri,
            icon = request.icon,
            gasLimit = "",
            inputType = when (outputType) {
                TransferDataOutputType.ENCODED_TRANSACTION -> ConfirmParams.TransferParams.InputType.EncodeTransaction
                TransferDataOutputType.SIGNATURE -> ConfirmParams.TransferParams.InputType.Signature
            },
            destination = DestinationAddress(""),
            amount = BigInteger.ZERO,
            isSendable = isSendable,
        )
    }
}