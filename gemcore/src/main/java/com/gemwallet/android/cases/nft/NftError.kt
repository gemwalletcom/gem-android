package com.gemwallet.android.cases.nft

sealed class NftError(message: String) : Exception(message) {
    object NotFoundAsset : NftError("Asset not found")
    object NotFoundCollection : NftError("Collection not found")
    object LoadError : NftError("Load error")
}