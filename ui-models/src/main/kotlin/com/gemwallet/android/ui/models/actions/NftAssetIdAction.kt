package com.gemwallet.android.ui.models.actions

fun interface NftAssetIdAction {
    operator fun invoke(id: String)
}