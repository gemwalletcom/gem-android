package com.gemwallet.features.asset.presents.chart.components

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.DrawContext
import com.patrykandpatrick.vico.core.common.component.Component
import com.patrykandpatrick.vico.core.common.component.PaintComponent
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.common.shape.Shape
import kotlin.math.roundToInt
import kotlin.properties.Delegates

/**
 * [ShapeComponent] is a [com.patrykandpatrick.vico.core.common.component.Component] that draws a shape.
 *
 * @param shape the [com.patrykandpatrick.vico.core.common.shape.Shape] that will be drawn.
 * @param color the color of the shape.
 * @param dynamicShader an optional [android.graphics.Shader] provider used as the shape’s background.
 * @param margins the [com.patrykandpatrick.vico.core.common.component.Component]’s margins.
 * @param strokeWidthDp the width of the shape’s stroke (in dp).
 * @param strokeColor the color of the stroke.
 */
open class ShapeComponent(
    val shape: Shape = Shape.Companion.Rectangle,
    color: Int = Color.BLACK,
    val dynamicShader: DynamicShader? = null,
    override val margins: Dimensions = Dimensions.Companion.Empty,
    val strokeWidthDp: Float = 0f,
    strokeColor: Int = Color.TRANSPARENT,
) : PaintComponent<ShapeComponent>(), Component {
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ALPHA_BIT_SHIFT = 24
    private val RED_BIT_SHIFT = 16
    private val GREEN_BIT_SHIFT = 8
    private val BLUE_BIT_SHIFT = 0
    internal val MAX_HEX_VALUE = 255f
    private val COLOR_MASK = 0xff

    private val strokePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    protected val path: Path = Path()

    /** The color of the shape. */
    public var color: Int by Delegates.observable(color) { _, _, value -> paint.color = value }

    /** The color of the stroke. */
    public var strokeColor: Int by
    Delegates.observable(strokeColor) { _, _, value -> strokePaint.color = value }

    init {
        paint.color = color

        with(strokePaint) {
            this.color = strokeColor
            style = Paint.Style.STROKE
        }
    }

    override fun draw(
        context: DrawContext,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        opacity: Float,
    ): Unit = with(context) {
            if (left == right || top == bottom) return // Skip drawing shape that will be invisible.
            path.rewind()
            applyShader(context, left, top, right, bottom)
            val centerX = (left + right) / 2
            val centerY = (top + bottom) / 2
            componentShadow.maybeUpdateShadowLayer(
                context = this,
                paint = paint,
                backgroundColor = color,
                opacity = opacity,
            )

            val strokeWidth = strokeWidthDp.pixels
            strokePaint.strokeWidth = strokeWidth

            fun drawShape(paint: Paint, isStroke: Boolean) = try {
                val strokeCompensation = if (isStroke) strokeWidth / 2 else 0f
                val left = minOf(left + margins.startDp.pixels + strokeWidth / 2, centerX - strokeCompensation)
                    .takeIf { it != Float.NaN }?.roundToInt()?.toFloat() ?: return
                val top = minOf(top + margins.topDp.pixels + strokeWidth / 2, centerY - strokeCompensation)
                    .takeIf { it != Float.NaN }?.roundToInt()?.toFloat() ?: return
                val right = maxOf(right - margins.endDp.pixels - strokeWidth / 2, centerX + strokeCompensation)
                    .takeIf { it != Float.NaN }?.roundToInt()?.toFloat() ?: return
                val bottom = maxOf(bottom - margins.bottomDp.pixels - strokeWidth / 2, centerY + strokeCompensation)
                    .takeIf { it != Float.NaN }?.roundToInt()?.toFloat() ?: return
                shape.drawShape(
                    context = context,
                    paint = paint,
                    path = path,
                    left = left,
                    top = top,
                    right = right,
                    bottom = bottom,
                )
            } catch (_: Throwable) {}

            paint.withOpacity(opacity) { paint -> drawShape(paint = paint, isStroke = false) }
            if (strokeWidth > 0f && strokeColor.alpha > 0) drawShape(paint = strokePaint, isStroke = true)
        }

    protected fun applyShader(
        context: DrawContext,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ) {
        dynamicShader?.provideShader(context, left, top, right, bottom)?.let { shader ->
            paint.shader = shader
        }
    }

    fun Paint.withOpacity(opacity: Float, action: (Paint) -> Unit) {
        val previousOpacity = this.alpha
        color = color.copyColor(opacity * previousOpacity / MAX_HEX_VALUE)
        action(this)
        this.alpha = previousOpacity
    }

    internal val Int.alpha: Int
        get() = extractColorChannel(ALPHA_BIT_SHIFT)

    private fun Int.extractColorChannel(bitShift: Int): Int = this shr bitShift and COLOR_MASK

    fun Int.copyColor(
        alpha: Float = this.extractColorChannel(ALPHA_BIT_SHIFT) / MAX_HEX_VALUE,
        red: Float = this.extractColorChannel(RED_BIT_SHIFT) / MAX_HEX_VALUE,
        green: Float = this.extractColorChannel(GREEN_BIT_SHIFT) / MAX_HEX_VALUE,
        blue: Float = this.extractColorChannel(BLUE_BIT_SHIFT) / MAX_HEX_VALUE,
    ): Int =
        copyColor(
            alpha = (alpha * MAX_HEX_VALUE).toInt(),
            red = (red * MAX_HEX_VALUE).toInt(),
            green = (green * MAX_HEX_VALUE).toInt(),
            blue = (blue * MAX_HEX_VALUE).toInt(),
        )

    fun Int.copyColor(
        alpha: Int = this.extractColorChannel(ALPHA_BIT_SHIFT),
        red: Int = this.extractColorChannel(RED_BIT_SHIFT),
        green: Int = this.extractColorChannel(GREEN_BIT_SHIFT),
        blue: Int = this.extractColorChannel(BLUE_BIT_SHIFT),
    ): Int =
        alpha shl
                ALPHA_BIT_SHIFT or
                (red shl RED_BIT_SHIFT) or
                (green shl GREEN_BIT_SHIFT) or
                (blue shl BLUE_BIT_SHIFT)
}