package com.gemwallet.android.features.widgets

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

class GemPricesWidgetReceiver : GlanceAppWidgetReceiver() {


    override val glanceAppWidget: GlanceAppWidget = PricesWidget()
}