package com.gemwallet.android.model

import com.gemwallet.android.ext.urlDecode
import com.gemwallet.android.ext.urlEncode
import com.gemwallet.android.serializer.jsonEncoder
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.TransactionType
import kotlinx.serialization.Serializable
import java.util.Base64

@Serializable
data class AmountParams(
    val assetId: AssetId,
    val txType: TransactionType,
    val destination: DestinationAddress? = null,
    val memo: String? = null,
    val validatorId: String? = null,
    val delegationId: String? = null,
) {

    fun pack(): String? {
        val json = jsonEncoder.encodeToString(this)
        return Base64.getEncoder().encodeToString(json.toByteArray()).urlEncode()
    }

    companion object {
        fun unpack(input: String): AmountParams? {
            val json = String(Base64.getDecoder().decode(input.urlDecode()))
            return jsonEncoder.decodeFromString<AmountParams>(json)
        }

//        private fun getGson(): Gson {
//            return GsonBuilder()
//                .registerTypeAdapter(AssetId::class.java, AssetIdSerializer())
//                .create()
//        }

        fun buildTransfer(
            assetId: AssetId,
            destination: DestinationAddress?,
            memo: String,
        ): AmountParams = AmountParams(
            assetId = assetId,
            txType = TransactionType.Transfer,
            destination = destination,
            memo = memo,
        )

        fun buildStake(
            assetId: AssetId,
            txType: TransactionType,
            validatorId: String? = null,
            delegationId: String? = null,
        ): AmountParams = AmountParams(
            assetId = assetId,
            txType = txType,
            delegationId = delegationId,
            validatorId = validatorId,
        )
    }
}
