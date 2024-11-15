package com.gemwallet.android.ui.models.actions

import com.wallet.core.primitives.AssetId


fun interface FinishConfirmAction {
    operator fun invoke(assetId: AssetId, hash: String)
}