package com.gemwallet.features.perpetual.views.components

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.domains.perpetual.aggregates.PerpetualPositionDetailsDataAggregate
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.InfoSheetEntity
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.list_item.priceColor
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.models.ListPosition

internal fun LazyListScope.positionProperties(position: PerpetualPositionDetailsDataAggregate?) {
    if (position == null) {
        return
    }
    item {
        SubheaderItem(R.string.perpetual_position)
    }
    item {
        PerpetualPositionItem(position, listPosition = ListPosition.First)
        PropertyItem(
            title = stringResource(R.string.perpetual_pnl),
            data = position.pnlWithPercentage,
            dataColor = priceColor(position.pnlState), // TODO: move price color to extend on pricestate
            listPosition = ListPosition.Middle,
        )
        PropertyItem(
            title = stringResource(R.string.perpetual_auto_close),
            data = position.autoClose,
            info = InfoSheetEntity.AutoCloseInfo,
            listPosition = ListPosition.Middle,
        )
        PropertyItem(
            title = stringResource(R.string.perpetual_size),
            data = position.size,
            listPosition = ListPosition.Middle,
        )
        PropertyItem(
            title = stringResource(R.string.perpetual_entry_price),
            data = position.entryPrice,
            listPosition = ListPosition.Middle,
        )
        PropertyItem(
            title = stringResource(R.string.info_liquidation_price_title),
            data = position.liquidationPrice,
            info = InfoSheetEntity.LiquidationPriceInfo,
            listPosition = ListPosition.Middle,
        )
        PropertyItem(
            title = stringResource(R.string.perpetual_margin),
            data = position.margin,
            listPosition = ListPosition.Middle,
        )
        PropertyItem(
            title = stringResource(R.string.info_funding_payments_title),
            data = position.fundingPayments,
            info = InfoSheetEntity.FundingPayments,
            listPosition = ListPosition.Last,
        )
    }
}