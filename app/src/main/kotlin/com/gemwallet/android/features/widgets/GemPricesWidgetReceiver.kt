package com.gemwallet.android.features.widgets

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class GemPricesWidgetReceiver : GlanceAppWidgetReceiver() {


    override val glanceAppWidget: GlanceAppWidget = PricesWidget()
}