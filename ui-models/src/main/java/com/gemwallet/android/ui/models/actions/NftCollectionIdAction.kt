package com.gemwallet.android.ui.models.actions

fun interface NftCollectionIdAction {
    operator fun invoke(id: String)
}