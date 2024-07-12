package com.gemwallet.android.features.amount.model

import com.gemwallet.android.ext.urlDecode
import com.gemwallet.android.ext.urlEncode
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.serializer.AssetIdSerializer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.TransactionType
import java.util.Base64

sealed class AmountParams(
    val assetId: AssetId,
    val txType: TransactionType,
) {
    open class TransferAmountParams(
        assetId: AssetId,
        val destinationAddress: String,
        val addressDomain: String,
        val memo: String,
    ) : AmountParams(assetId, TransactionType.Transfer)

    open class StakeAmountParams(
        assetId: AssetId,
        txType: TransactionType,
        val validatorId: String?,
        val delegationId: String?,
    ) : AmountParams(assetId, txType)

    fun pack(): String? {
        val json = getGson().toJson(this)
        return Base64.getEncoder().encodeToString(json.toByteArray()).urlEncode()
    }

    companion object {
        fun <T : ConfirmParams> unpack(type: Class<T>, input: String): T? {
            val json = String(Base64.getDecoder().decode(input.urlDecode()))
            return getGson().fromJson(json, type)
        }

        private fun getGson(): Gson {
            return GsonBuilder()
                .registerTypeAdapter(AssetId::class.java, AssetIdSerializer())
                .create()
        }
    }
}
