package com.wallet.core.primitives

import com.gemwallet.android.serializer.AssetIdSerializer
import kotlinx.serialization.Serializable

@Serializable(with = AssetIdSerializer::class)
data class AssetId (
	val chain: Chain,
	val tokenId: String? = null
) {
	override fun equals(other: Any?): Boolean {
		return (other as? AssetId)?.let {
            it.chain == chain && it.tokenId == tokenId
        } == true
	}

	override fun hashCode(): Int {
		var result = chain.hashCode()
		result = 31 * result + (tokenId?.hashCode() ?: 0)
		return result
	}

}