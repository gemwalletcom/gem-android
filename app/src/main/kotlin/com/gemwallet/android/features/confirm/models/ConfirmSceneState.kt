package com.gemwallet.android.features.confirm.models

import androidx.annotation.StringRes
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.ui.components.CellEntity
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.TransactionType

sealed interface ConfirmSceneState {

    data object Loading : ConfirmSceneState

    class Fatal(val error: ConfirmError) : ConfirmSceneState

    class Loaded(
        val type: TransactionType,
        val sending: Boolean,
        @StringRes val title: Int,
        val amount: String,
        val amountEquivalent: String,
        val fromAsset: AssetInfo,
        val toAsset: AssetInfo?,
        val fromAmount: String?,
        val toAmount: String?,
        val currency: Currency,
        val cells: List<CellEntity<Int>>,
        val txHash: String?,
        val error: ConfirmError,
    ) : ConfirmSceneState
}