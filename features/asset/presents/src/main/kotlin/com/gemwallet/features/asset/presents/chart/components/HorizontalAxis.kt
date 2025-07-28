package com.gemwallet.features.asset.presents.chart.components

import android.graphics.RectF
import androidx.annotation.RestrictTo
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.gemwallet.android.ui.components.designsystem.padding8
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.of
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.Insets
import com.patrykandpatrick.vico.core.cartesian.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.cartesian.axis.AxisPosition
import com.patrykandpatrick.vico.core.cartesian.axis.BaseAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.HorizontalPosition
import com.patrykandpatrick.vico.core.common.VerticalPosition
import com.patrykandpatrick.vico.core.common.component.TextComponent
import kotlin.math.ceil

internal inline val Float.ceil: Float
    get() = ceil(this)

internal inline val Float.doubled: Float
    get() = this * 2

internal fun RectF.getStart(isLtr: Boolean): Float = if (isLtr) left else right

public inline val Float.half: Float
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    get() = this / 2

internal fun <T : Comparable<T>> T.isBoundOf(range: ClosedFloatingPointRange<T>) =
    this == range.start || this == range.endInclusive

open class HorizontalAxis<Position : AxisPosition.Horizontal>(
    override val position: Position,
) : BaseAxis<Position>() {
    protected val AxisPosition.Horizontal.textVerticalPosition: VerticalPosition
        get() = if (isBottom) VerticalPosition.Bottom else VerticalPosition.Top

    /**
     * Determines for what _x_ values this [HorizontalAxis] is to display labels, ticks, and guidelines.
     */
    public var itemPlacer: AxisItemPlacer.Horizontal = AxisItemPlacer.Horizontal.default()

    override fun drawBehindChart(context: CartesianDrawContext): Unit = with(context) {
        val clipRestoreCount = canvas.save()
        val tickMarkTop = if (position.isBottom) bounds.top else bounds.bottom - axisThickness - tickLength
        val tickMarkBottom = tickMarkTop + axisThickness + tickLength
        val fullXRange = getFullXRange(horizontalDimensions)
        val maxLabelWidth = getMaxLabelWidth(horizontalDimensions, fullXRange)

        canvas.clipRect(
            bounds.left -
                    itemPlacer.getStartHorizontalAxisInset(this, horizontalDimensions, tickThickness, maxLabelWidth),
            minOf(bounds.top, chartBounds.top),
            bounds.right +
                    itemPlacer.getEndHorizontalAxisInset(this, horizontalDimensions, tickThickness, maxLabelWidth),
            maxOf(bounds.bottom, chartBounds.bottom),
        )

        val textY = if (position.isBottom) tickMarkBottom else tickMarkTop
        val baseCanvasX =
            bounds.getStart(isLtr) - horizontalScroll + horizontalDimensions.startPadding *
                    layoutDirectionMultiplier
        val firstVisibleX =
            fullXRange.start + horizontalScroll / horizontalDimensions.xSpacing * chartValues.xStep *
                    layoutDirectionMultiplier
        val lastVisibleX = firstVisibleX + bounds.width() / horizontalDimensions.xSpacing * chartValues.xStep
        val visibleXRange = firstVisibleX..lastVisibleX
        val labelValues = itemPlacer.getLabelValues(this, visibleXRange, fullXRange, maxLabelWidth)

        labelValues.forEachIndexed { _, x ->
            val canvasX =
                baseCanvasX + (x - chartValues.minX) / chartValues.xStep * horizontalDimensions.xSpacing *
                        layoutDirectionMultiplier
            val maxWidth = Int.MAX_VALUE

            label?.drawText(
                context = context,
                text = valueFormatter.format(value = x, chartValues = chartValues, verticalAxisPosition = null),
                textX = canvasX,
                textY = textY,
                verticalPosition = position.textVerticalPosition,
                horizontalPosition = when {
                    canvasX < bounds.width() * 0.20f -> HorizontalPosition.End
                    canvasX > bounds.width() * 0.80f -> HorizontalPosition.Start
                    else -> HorizontalPosition.Center
                },
                maxTextWidth = maxWidth,
                maxTextHeight = (bounds.height() - tickLength - axisThickness / 2).toInt(),
                rotationDegrees = labelRotationDegrees,
            )
        }

        if (clipRestoreCount >= 0) canvas.restoreToCount(clipRestoreCount)
    }

    override fun drawAboveChart(context: CartesianDrawContext): Unit = Unit

    override fun updateHorizontalDimensions(
        context: CartesianMeasureContext,
        horizontalDimensions: MutableHorizontalDimensions,
    ) {
        val label = label ?: return
        val chartValues = context.chartValues
        val maxLabelWidth = context.getMaxLabelWidth(horizontalDimensions, context.getFullXRange(horizontalDimensions))
        val firstLabelValue = itemPlacer.getFirstLabelValue(context, maxLabelWidth)
        val lastLabelValue = itemPlacer.getLastLabelValue(context, maxLabelWidth)
        if (firstLabelValue != null) {
            val text =
                valueFormatter.format(value = firstLabelValue, chartValues = chartValues, verticalAxisPosition = null)
            (label
                .getWidth(context = context, text = text, rotationDegrees = labelRotationDegrees, pad = true) / 2)
                .let { horizontalDimensions.ensureValuesAtLeast(unscalableStartPadding = it) }
        }
        if (lastLabelValue != null) {
            val text =
                valueFormatter.format(value = lastLabelValue, chartValues = chartValues, verticalAxisPosition = null)
            (label
                .getWidth(context = context, text = text, rotationDegrees = labelRotationDegrees, pad = true) / 2)
                .let { horizontalDimensions.ensureValuesAtLeast(unscalableEndPadding = it) }
        }
    }

    override fun getInsets(
        context: CartesianMeasureContext,
        outInsets: Insets,
        horizontalDimensions: HorizontalDimensions,
    ) {
        val maxLabelWidth = context.getMaxLabelWidth(horizontalDimensions, context.getFullXRange(horizontalDimensions))
        with(outInsets) {
            start =
                itemPlacer.getStartHorizontalAxisInset(
                    context,
                    horizontalDimensions,
                    context.tickThickness,
                    maxLabelWidth,
                )
            end =
                itemPlacer.getEndHorizontalAxisInset(
                    context,
                    horizontalDimensions,
                    context.tickThickness,
                    maxLabelWidth,
                )
            top = if (position.isTop) getDesiredHeight(context, horizontalDimensions, maxLabelWidth) else 0f
            bottom = if (position.isBottom) getDesiredHeight(context, horizontalDimensions, maxLabelWidth) else 0f
        }
    }

    protected fun CartesianMeasureContext.getFullXRange(
        horizontalDimensions: HorizontalDimensions,
    ): ClosedFloatingPointRange<Float> =
        with(horizontalDimensions) {
            val start = chartValues.minX - startPadding / xSpacing * chartValues.xStep
            val end = chartValues.maxX + endPadding / xSpacing * chartValues.xStep
            start..end
        }

    protected open fun getDesiredHeight(
        context: CartesianMeasureContext,
        horizontalDimensions: HorizontalDimensions,
        maxLabelWidth: Float,
    ): Float =
        with(context) {
            val fullXRange = getFullXRange(horizontalDimensions)

            when (val constraint = sizeConstraint) {
                is SizeConstraint.Auto -> {
                    val labelHeight = getMaxLabelHeight(horizontalDimensions, fullXRange, maxLabelWidth)
                    val titleComponentHeight =
                        title?.let { title ->
                            titleComponent?.getHeight(
                                context = context,
                                width = bounds.width().toInt(),
                                text = title,
                            )
                        } ?: 0f
                    (labelHeight + titleComponentHeight + (if (position.isBottom) axisThickness else 0f) + tickLength)
                        .coerceAtMost(maximumValue = canvasBounds.height() / MAX_HEIGHT_DIVISOR)
                        .coerceIn(
                            minimumValue = constraint.minSizeDp.pixels,
                            maximumValue = constraint.maxSizeDp.pixels,
                        )
                }

                is SizeConstraint.Exact -> constraint.sizeDp.pixels
                is SizeConstraint.Fraction -> canvasBounds.height() * constraint.fraction
                is SizeConstraint.TextWidth ->
                    label?.getHeight(
                        context = this,
                        text = constraint.text,
                        rotationDegrees = labelRotationDegrees,
                    ) ?: 0f
            }
        }

    protected fun CartesianMeasureContext.getMaxLabelWidth(
        horizontalDimensions: HorizontalDimensions,
        fullXRange: ClosedFloatingPointRange<Float>,
    ): Float {
        val label = label ?: return 0f
        return itemPlacer
            .getWidthMeasurementLabelValues(this, horizontalDimensions, fullXRange)
            .maxOfOrNull { value ->
                val text = valueFormatter.format(value = value, chartValues = chartValues, verticalAxisPosition = null)
                label.getWidth(context = this, text = text, rotationDegrees = labelRotationDegrees, pad = true)
            } ?: 0f
    }

    protected fun CartesianMeasureContext.getMaxLabelHeight(
        horizontalDimensions: HorizontalDimensions,
        fullXRange: ClosedFloatingPointRange<Float>,
        maxLabelWidth: Float,
    ): Float {
        val label = label ?: return 0f
        return itemPlacer
            .getHeightMeasurementLabelValues(this, horizontalDimensions, fullXRange, maxLabelWidth)
            .maxOf { value ->
                val text = valueFormatter.format(value = value, chartValues = chartValues, verticalAxisPosition = null)
                label.getHeight(context = this, text = text, rotationDegrees = labelRotationDegrees, pad = true)
            }
    }

    /** Creates [HorizontalAxis] instances. It's recommended to use this via [HorizontalAxis.build]. */
    public class Builder<Position : AxisPosition.Horizontal>(
        builder: BaseAxis.Builder<Position>? = null,
    ) : BaseAxis.Builder<Position>(builder) {
        /**
         * Determines for what _x_ values the [HorizontalAxis] is to display labels, ticks, and guidelines.
         */
        public var itemPlacer: AxisItemPlacer.Horizontal = AxisItemPlacer.Horizontal.default()

        /**
         * Creates a [HorizontalAxis] instance with the properties from this [Builder].
         */
        @Suppress("UNCHECKED_CAST")
        public inline fun <reified T : Position> build(): HorizontalAxis<T> {
            val position =
                when (T::class.java) {
                    AxisPosition.Horizontal.Top::class.java -> AxisPosition.Horizontal.Top
                    AxisPosition.Horizontal.Bottom::class.java -> AxisPosition.Horizontal.Bottom
                    else -> throw IllegalStateException("Got unknown AxisPosition class ${T::class.java.name}")
                } as Position
            return setTo(HorizontalAxis(position = position)).also { it.itemPlacer = itemPlacer } as HorizontalAxis<T>
        }

        public fun <Position : AxisPosition, A : BaseAxis<Position>> BaseAxis.Builder<Position>.setTo(axis: A): A {
            axis.axisLine = this.axis
            axis.tick = tick
            axis.guideline = guideline
            axis.label = label
            axis.tickLengthDp = tickLengthDp
            axis.valueFormatter = valueFormatter
            axis.sizeConstraint = sizeConstraint
            axis.titleComponent = titleComponent
            axis.title = title
            axis.labelRotationDegrees = labelRotationDegrees
            return axis
        }
    }

    /** Houses a [HorizontalAxis] factory function. */
    public companion object {
        private const val MAX_HEIGHT_DIVISOR = 3f

        /** Creates a [HorizontalAxis] via [Builder]. */
        public inline fun <reified P : AxisPosition.Horizontal> build(
            block: Builder<P>.() -> Unit = {},
        ): HorizontalAxis<P> = Builder<P>().apply(block).build()
    }
}

@Composable
fun rememberTopAxis(
    label: TextComponent? = rememberAxisLabelComponent(),
    valueFormatter: CartesianValueFormatter = remember { CartesianValueFormatter.decimal() },
    itemPlacer: AxisItemPlacer.Horizontal = remember { AxisItemPlacer.Horizontal.default() },
): HorizontalAxis<AxisPosition.Horizontal.Top> = remember { HorizontalAxis.build<AxisPosition.Horizontal.Top>() }.apply {
    this.label = rememberTextComponent(
        color = MaterialTheme.colorScheme.secondary,
        margins = Dimensions.of(vertical = padding8)
    )
    axisLine = null
    this.tick = null
    this.guideline = null
    this.valueFormatter = valueFormatter
    tickLengthDp = 4f
    this.sizeConstraint = BaseAxis.SizeConstraint.Auto()
    this.labelRotationDegrees = 0f
    this.titleComponent = null
    this.title = null
    this.itemPlacer = itemPlacer
}

@Composable
fun rememberBottomAxis(
    label: TextComponent? = rememberAxisLabelComponent(),
    valueFormatter: CartesianValueFormatter = remember { CartesianValueFormatter.decimal() },
    itemPlacer: AxisItemPlacer.Horizontal = remember { AxisItemPlacer.Horizontal.default() },
): HorizontalAxis<AxisPosition.Horizontal.Bottom> = remember { HorizontalAxis.build<AxisPosition.Horizontal.Bottom>() }.apply {
    this.label = rememberTextComponent(
        color = MaterialTheme.colorScheme.secondary,
        margins = Dimensions.of(vertical = padding8)
    )
    axisLine = null
    this.tick = null
    this.guideline = null
    this.valueFormatter = valueFormatter
    tickLengthDp = 4f
    this.sizeConstraint = BaseAxis.SizeConstraint.Auto()
    this.labelRotationDegrees = 0f
    this.titleComponent = null
    this.title = null
    this.itemPlacer = itemPlacer
}