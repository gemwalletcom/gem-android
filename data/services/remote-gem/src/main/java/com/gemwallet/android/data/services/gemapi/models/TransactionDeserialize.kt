package com.gemwallet.android.data.services.gemapi.models

import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import com.gemwallet.android.ext.findByString
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Transaction
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionInput
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionType
import java.lang.reflect.Type

class TransactionDeserialize : JsonDeserializer<Transaction?> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext?
    ): Transaction? {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        format.timeZone = TimeZone.getTimeZone("GMT")
        return toTransaction(json, format)
    }
    companion object {
        fun toTransaction(jsonElement: JsonElement, format: DateFormat): Transaction? {
            val jObj = jsonElement.asJsonObject
            val assetIdJson = jObj["assetId"].asJsonObject
            val feeAssetIdJson = jObj["feeAssetId"].asJsonObject
            val assetId = AssetId(
                chain = Chain.findByString(assetIdJson["chain"].asString ?: return null) ?: return null,
                tokenId = if (assetIdJson["tokenId"].isJsonNull) null else assetIdJson["tokenId"].asString,
            )
            val feeAssetId = AssetId(
                chain = Chain.findByString(feeAssetIdJson["chain"].asString ?: return null) ?: return null,
                tokenId = if (feeAssetIdJson["tokenId"].isJsonNull) null else feeAssetIdJson["tokenId"].asString,
            )
            return Transaction(
                id = jObj["id"].asString,
                hash = jObj["hash"].asString,
                assetId = assetId,
                from = jObj["from"].asString,
                to = jObj["to"].asString,
                contract = if (jObj["contract"].isJsonNull) null else jObj["contract"].asString,
                type = when (jObj["type"].asString) {
                    TransactionType.Transfer.string -> TransactionType.Transfer
                    TransactionType.Swap.string -> TransactionType.Swap
                    TransactionType.TokenApproval.string -> TransactionType.TokenApproval
                    TransactionType.StakeDelegate.string -> TransactionType.StakeDelegate
                    TransactionType.StakeRedelegate.string -> TransactionType.StakeRedelegate
                    TransactionType.StakeRewards.string -> TransactionType.StakeRewards
                    TransactionType.StakeUndelegate.string -> TransactionType.StakeUndelegate
                    TransactionType.StakeWithdraw.string -> TransactionType.StakeWithdraw
                    else -> return null
                },
                state = when (jObj["state"].asString) {
                    TransactionState.Pending.string -> TransactionState.Pending
                    TransactionState.Confirmed.string -> TransactionState.Confirmed
                    TransactionState.Reverted.string -> TransactionState.Reverted
                    TransactionState.Failed.string -> TransactionState.Failed
                    else -> return null
                },
                blockNumber = jObj["blockNumber"].asString,
                sequence = jObj["sequence"].asString,
                fee = jObj["fee"].asString,
                feeAssetId = feeAssetId,
                value = jObj["value"].asString,
                memo = if (jObj["memo"].isJsonNull) null else jObj["memo"].asString,
                direction = when (jObj["direction"].asString) {
                    TransactionDirection.Outgoing.string -> TransactionDirection.Outgoing
                    TransactionDirection.SelfTransfer.string -> TransactionDirection.SelfTransfer
                    TransactionDirection.Incoming.string -> TransactionDirection.Incoming
                    else -> return null
                },
                metadata = if (jObj["metadata"].isJsonNull) null else jObj["metadata"].asJsonObject.toString(),
                utxoInputs = jObj["utxoInputs"]?.asJsonArray?.map {
                    TransactionInput(
                        it.asJsonObject["address"].asString,
                        it.asJsonObject["value"].asString,
                    )
                } ?: emptyList(),
                utxoOutputs = jObj["utxoOutputs"]?.asJsonArray?.map {
                    TransactionInput(
                        it.asJsonObject["address"].asString,
                        it.asJsonObject["value"].asString,
                    )
                } ?: emptyList(),
                createdAt = try {
                    format.parse(jObj["createdAt"].asString).time
                } catch (err: Throwable) {
                    return null
                },
            )
        }
    }
}