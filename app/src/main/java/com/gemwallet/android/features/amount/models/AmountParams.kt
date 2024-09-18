package com.gemwallet.android.features.amount.models

import com.gemwallet.android.ext.urlDecode
import com.gemwallet.android.ext.urlEncode
import com.gemwallet.android.model.DestinationAddress
import com.gemwallet.android.serializer.AssetIdSerializer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.TransactionType
import java.util.Base64

data class AmountParams(
    val assetId: AssetId,
    val txType: TransactionType,
    val destination: DestinationAddress? = null,
    val memo: String? = null,
    val validatorId: String? = null,
    val delegationId: String? = null,
) {

    fun pack(): String? {
        val json = getGson().toJson(this)
        return Base64.getEncoder().encodeToString(json.toByteArray()).urlEncode()
    }

    companion object {
        fun unpack(input: String): AmountParams? {
            val json = String(Base64.getDecoder().decode(input.urlDecode()))
            return getGson().fromJson(json, AmountParams::class.java)
        }

        private fun getGson(): Gson {
            return GsonBuilder()
                .registerTypeAdapter(AssetId::class.java, AssetIdSerializer())
                .create()
        }

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
