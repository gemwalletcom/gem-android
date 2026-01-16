package com.gemwallet.android.domains.pricealerts.values

import com.wallet.core.primitives.AssetId

sealed class PriceAlertsStateEvent(val assetId: AssetId? = null) {

    class Request(assetId: AssetId? = null) : PriceAlertsStateEvent(assetId)

    class Disable(assetId: AssetId? = null) : PriceAlertsStateEvent(assetId)

    class Enable(assetId: AssetId? = null) : PriceAlertsStateEvent(assetId)

    class PushRequested(assetId: AssetId? = null) : PriceAlertsStateEvent(assetId)

    class PushGranted(assetId: AssetId? = null) : PriceAlertsStateEvent(assetId)

    class PushRejected(assetId: AssetId? = null) : PriceAlertsStateEvent(assetId)
}