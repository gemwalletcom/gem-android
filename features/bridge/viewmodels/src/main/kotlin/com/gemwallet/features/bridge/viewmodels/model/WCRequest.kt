package com.gemwallet.features.bridge.viewmodels.model

import androidx.core.net.toUri
import com.gemwallet.android.blockchain.services.SignClientProxy
import com.gemwallet.android.data.repositoreis.bridge.getNamespace
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.getShortUrl
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.math.hexToBigInteger
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.DestinationAddress
import com.gemwallet.android.serializer.jsonEncoder
import com.reown.walletkit.client.Wallet
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WCSolanaSignMessage
import com.wallet.core.primitives.WCSolanaSignMessageResult
import com.wallet.core.primitives.WCSolanaTransaction
import com.wallet.core.primitives.WCSuiSignMessage
import com.wallet.core.primitives.WCSuiSignMessageResult
import com.wallet.core.primitives.WCSuiTransaction
import com.wallet.core.primitives.WalletConnectionMethods
import org.json.JSONArray
import uniffi.gemstone.SignDigestType
import uniffi.gemstone.SignMessage
import uniffi.gemstone.SignMessageDecoder
import wallet.core.jni.Base64
import java.math.BigInteger
import kotlin.collections.isEmpty

sealed class WCRequest(
    internal val data: Wallet.Model.SessionRequest,
    internal val account: Account
) {
    val name: String get() = data.peerMetaData?.name ?: ""

    val icon: String get() = data.peerMetaData?.icons?.firstOrNull() ?: ""
    val description: String get() = data.peerMetaData?.description ?: ""
    val uri: String get() = data.peerMetaData?.url?.getShortUrl() ?: ""
    val method: String get() = data.request.method
    abstract val params: String

    val chain: Chain get() = account.chain

    open suspend fun sign(signClient: SignClientProxy, privateKey: ByteArray): String = ""

    abstract class SignMessage(data: Wallet.Model.SessionRequest, account: Account) : WCRequest(data, account) {
        abstract val signDigestType: SignDigestType

        val decoder: SignMessageDecoder
            get() = SignMessageDecoder(
                SignMessage(signDigestType, params.toByteArray())
            )

        class EthSign(data: Wallet.Model.SessionRequest, account: Account) : SignMessage(data, account) {
            override val signDigestType: SignDigestType
                get() = SignDigestType.SIGN

            override suspend fun sign(
                signClient: SignClientProxy,
                privateKey: ByteArray
            ): String {
                val param = decoder.hash()
                return signClient.signMessage(chain, param, privateKey).toHexString()
            }

            override val params: String
                get() {
                    val data = JSONArray(data.request.params).getString(1)
                    return String(data.decodeHex())
                }
        }

        class EthSignTypedData(data: Wallet.Model.SessionRequest, account: Account) : SignMessage(data, account) {

            override val signDigestType: SignDigestType
                get() = SignDigestType.EIP712

            override val params: String
                get() {
                    val data = JSONArray(data.request.params).getString(1)
                    return data
                }

            override suspend fun sign(
                signClient: SignClientProxy,
                privateKey: ByteArray
            ): String {
                return signClient.signTypedMessage(chain, params.toByteArray(), privateKey).toHexString()
            }
        }

        class PersonalSign(data: Wallet.Model.SessionRequest, account: Account) : SignMessage(data, account) {

            override val signDigestType: SignDigestType
                get() = SignDigestType.EIP191

            override val params: String
                get() {
                    val data = JSONArray(data.request.params).getString(0)
                    return try {
                        String(data.decodeHex())
                    } catch (_: Throwable) {
                        data
                    }
                }

            override suspend fun sign(
                signClient: SignClientProxy,
                privateKey: ByteArray
            ): String {
                val param = decoder.hash()
                return signClient.signMessage(chain, param, privateKey).toHexString()
            }
        }

        class SolanaSignMessage(data: Wallet.Model.SessionRequest, account: Account) : SignMessage(data, account) {

            override val signDigestType: SignDigestType
                get() = SignDigestType.BASE58

            override val params: String
                get() {
                    val data = jsonEncoder.decodeFromString<WCSolanaSignMessage>(data.request.params)
                    return data.message
                }

            override suspend fun sign(
                signClient: SignClientProxy,
                privateKey: ByteArray
            ): String {
                val sign = signClient.signMessage(chain, decoder.hash(), privateKey)//.toHexString()
                val result = decoder.getResult(sign)
                return jsonEncoder.encodeToString(WCSolanaSignMessageResult(signature = result))
            }
        }

        class SuiSignPersonalMessage(data: Wallet.Model.SessionRequest, account: Account) : SignMessage(data, account) {

            override val signDigestType: SignDigestType
                get() = SignDigestType.SUI_PERSONAL_MESSAGE

            override val params: String
                get() {
                    return data.request.params
                }

            override suspend fun sign(
                signClient: SignClientProxy,
                privateKey: ByteArray
            ): String {
                val message = jsonEncoder.decodeFromString<WCSuiSignMessage>(params)
                val data = Base64.decode(message.message)
                val input = if (data == null || data.isEmpty()) {
                    params.toByteArray()
                } else {
                    data
                }
                val signature = String(signClient.signMessage(chain, input, privateKey))
                val result = WCSuiSignMessageResult(signature)

                return jsonEncoder.encodeToString(result)
            }
        }
    }

    abstract class Transaction (data: Wallet.Model.SessionRequest, account: Account) : WCRequest(data, account) {

        abstract val inputType: ConfirmParams.TransferParams.InputType

        open val destination: DestinationAddress get() = DestinationAddress("")

        open val amount: BigInteger get() = BigInteger.ZERO

        open val confirmParams: ConfirmParams.TransferParams.Generic
            get() = ConfirmParams.TransferParams.Generic(
                requestId = data.request.id.toString(),
                asset = chain.asset(),
                from = account,
                memo = params,
                name = name,
                description = description,
                url = uri,
                icon = icon,
                gasLimit = "",
                inputType = inputType,
                destination = destination,
                amount = amount,
            )

        abstract class SignTransaction(data: Wallet.Model.SessionRequest, account: Account) : Transaction(data, account) {

            override val inputType: ConfirmParams.TransferParams.InputType
                get() = ConfirmParams.TransferParams.InputType.Signature

            class EthSignTransaction(data: Wallet.Model.SessionRequest, account: Account) : SignTransaction(data, account) {
                override val params: String
                    get() {
                        val jObj = JSONArray(data.request.params).getJSONObject(0)
                        return jObj.getString("data")
                    }

                override val destination: DestinationAddress
                    get() {
                        val jObj = JSONArray(data.request.params).getJSONObject(0)
                        return DestinationAddress(address = jObj.getString("to"))
                    }

                override val amount: BigInteger
                    get() {
                        val jObj = JSONArray(data.request.params).getJSONObject(0)
                        return jObj.optString("value", "0x0").hexToBigInteger() ?: BigInteger.ZERO
                    }
            }

            class SolanaSignTransaction(data: Wallet.Model.SessionRequest, account: Account) : SignTransaction(data, account) {
                override val params: String
                    get() {
                        val data = jsonEncoder.decodeFromString<WCSolanaTransaction>(data.request.params)
                        return data.transaction
                    }
            }

            class SuiSignTransaction(data: Wallet.Model.SessionRequest, account: Account) : SignTransaction(data, account) {
                override val params: String
                    get() {
                        val data = jsonEncoder.decodeFromString<WCSuiTransaction>(data.request.params)
                        return data.transaction
                    }
            }
        }

        abstract class SendTransaction(data: Wallet.Model.SessionRequest, account: Account) : Transaction(data, account) {

            override val inputType: ConfirmParams.TransferParams.InputType
                get() = ConfirmParams.TransferParams.InputType.EncodeTransaction

            class EthSendTransaction(data: Wallet.Model.SessionRequest, account: Account) : SendTransaction(data, account) {

                override val params: String
                    get() {
                        val jObj = JSONArray(data.request.params).getJSONObject(0)
                        return jObj.getString("data")
                    }

                override val destination: DestinationAddress
                    get() {
                        val jObj = JSONArray(data.request.params).getJSONObject(0)
                        return DestinationAddress(address = jObj.getString("to"))
                    }

                override val amount: BigInteger
                    get() {
                        val jObj = JSONArray(data.request.params).getJSONObject(0)
                        return jObj.optString("value", "0x0").hexToBigInteger() ?: BigInteger.ZERO
                    }
            }

            class SolanaSignAndSendTransaction(
                data: Wallet.Model.SessionRequest,
                account: Account
            ) : SendTransaction(data, account) {
                override val params: String
                    get() {
                        val data = jsonEncoder.decodeFromString<WCSolanaTransaction>(data.request.params)
                        return data.transaction
                    }
            }

            class SuiSignAndExecuteTransaction(
                data: Wallet.Model.SessionRequest,
                account: Account
            ) : SendTransaction(data, account) {
                override val params: String
                    get() {
                        val data = jsonEncoder.decodeFromString<WCSuiTransaction>(data.request.params)
                        return data.transaction
                    }
            }
        }
    }

    class WalletSwitchEthereumChain(data: Wallet.Model.SessionRequest, account: Account) : WCRequest(data, account) {
        override val params: String
            get() = ""
    }
}

fun Wallet.Model.SessionRequest.map(wallet: com.wallet.core.primitives.Wallet): WCRequest {
    val chain = Chain.getNamespace(chainId)
    if (chain == null) {
        throw IllegalArgumentException("chain not supported")
    }
    val account: Account = wallet.getAccount(chain) ?: throw IllegalArgumentException("chain not supported")
    return when (this.request.method) {
        WalletConnectionMethods.EthSign.string -> WCRequest.SignMessage.EthSign(this, account)
        WalletConnectionMethods.EthSignTypedData.string,
        WalletConnectionMethods.EthSignTypedDataV4.string -> WCRequest.SignMessage.EthSignTypedData(this, account)

        WalletConnectionMethods.PersonalSign.string -> WCRequest.SignMessage.PersonalSign(this, account)
        WalletConnectionMethods.SolanaSignMessage.string -> WCRequest.SignMessage.SolanaSignMessage(this, account)

        WalletConnectionMethods.EthSignTransaction.string -> WCRequest.Transaction.SignTransaction.EthSignTransaction(this, account)

        WalletConnectionMethods.EthSendTransaction.string -> WCRequest.Transaction.SendTransaction.EthSendTransaction(this, account)

        WalletConnectionMethods.SolanaSignAndSendTransaction.string -> WCRequest.Transaction.SendTransaction.SolanaSignAndSendTransaction(this, account)

        WalletConnectionMethods.SolanaSignTransaction.string -> WCRequest.Transaction.SignTransaction.SolanaSignTransaction(this, account)

        WalletConnectionMethods.WalletSwitchEthereumChain.string -> WCRequest.WalletSwitchEthereumChain(this, account)

        WalletConnectionMethods.SuiSignPersonalMessage.string -> WCRequest.SignMessage.SuiSignPersonalMessage(this, account)

        WalletConnectionMethods.SuiSignTransaction.string -> WCRequest.Transaction.SignTransaction.SuiSignTransaction(this, account)

        WalletConnectionMethods.SuiSignAndExecuteTransaction.string -> WCRequest.Transaction.SendTransaction.SuiSignAndExecuteTransaction(this, account)

        else -> throw IllegalArgumentException("Unsupported method: ${this.request.method}")
    }
}