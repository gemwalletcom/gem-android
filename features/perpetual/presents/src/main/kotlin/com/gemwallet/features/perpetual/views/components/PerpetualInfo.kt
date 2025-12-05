package com.gemwallet.features.perpetual.views.components

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.domains.perpetual.aggregates.PerpetualDetailsDataAggregate
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.InfoSheetEntity
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.models.ListPosition

fun LazyListScope.perpetualInfo(data: PerpetualDetailsDataAggregate) {
    item { SubheaderItem(stringResource(R.string.common_info)) }

    item {
        PropertyItem(
            title = stringResource(R.string.markets_daily_volume),
            data = data.dayVolume,
            listPosition = ListPosition.First,
        )
        PropertyItem(
            title = stringResource(R.string.info_open_interest_title),
            data = data.openInterest,
            info = InfoSheetEntity.OpenInterestInfo,
            listPosition = ListPosition.Middle,
        )
        PropertyItem(
            title = stringResource(R.string.info_funding_rate_title),
            data = data.funding,
            info = InfoSheetEntity.FundingInfo,
            listPosition = ListPosition.Last,
        )
    }
}