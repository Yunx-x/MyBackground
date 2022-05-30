package com.akx.background

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import java.lang.reflect.Field

class Shape(context: Context, attr: TypedArray) :
    IBackgroundCreator(context, attr) {

    companion object {
        val styleableId = R.styleable.BackgroundShape
    }

    override fun createDrawable(): Drawable? {
        val hasOrNo = attr.indexCount > 0
        return if (hasOrNo) {
            val drawable = GradientDrawable()
            val shape =
                attr.getInt(R.styleable.BackgroundShape_shape_name, GradientDrawable.RECTANGLE)
            drawable.shape = shape
            setCorners(drawable, attr)
            setGradient(drawable, attr)
            setPadding(drawable, attr)
            setSize(drawable, attr)
            setSolid(drawable, attr)
            setStroke(drawable, attr)
            drawable
        } else {
            null
        }
    }

    private fun setCorners(
        drawable: GradientDrawable,
        attr: TypedArray
    ) {
        val cornersRadius =
            attr.getDimension(R.styleable.BackgroundShape_shape_corners_radius, 0f)
        drawable.cornerRadius = cornersRadius

        val cornersLTr =
            attr.getDimension(R.styleable.BackgroundShape_shape_corners_topLeftRadius, 0f)
        val cornersLRr =
            attr.getDimension(R.styleable.BackgroundShape_shape_corners_topRightRadius, 0f)
        val cornersBLr =
            attr.getDimension(
                R.styleable.BackgroundShape_shape_corners_bottomLeftRadius,
                0f
            )
        val cornersBRr =
            attr.getDimension(
                R.styleable.BackgroundShape_shape_corners_bottomRightRadius,
                0f
            )
        val cornerRadii = floatArrayOf(
            cornersLTr, cornersLTr,//左上
            cornersLRr, cornersLRr,//右上
            cornersBLr, cornersBLr,//左下
            cornersBRr, cornersBRr//右下
        )
        drawable.cornerRadii = cornerRadii
    }

    private fun setGradient(
        drawable: GradientDrawable,
        attr: TypedArray
    ) {
        val type = attr.getInt(
            R.styleable.BackgroundShape_shape_gradient_type,
            GradientDrawable.LINEAR_GRADIENT
        )
        drawable.gradientType = type

        val useLevel =
            attr.getBoolean(R.styleable.BackgroundShape_shape_gradient_useLevel, false)
        drawable.useLevel = useLevel

        var angle = attr.getInt(R.styleable.BackgroundShape_shape_gradient_angle, 0)
        // GradientDrawable historically has not parsed negative angle measurements and always
        // stays on the default orientation for API levels older than Q.
        // Only configure the orientation if the angle is greater than zero.
        // Otherwise fallback on Orientation.TOP_BOTTOM
        // In Android Q and later, actually wrap the negative angle measurement to the correct
        // value
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            angle = (angle % 360 + 360) % 360 // offset negative angle measures
        } else {
            angle %= 360
        }

        val orientation = when (angle) {
            0 -> GradientDrawable.Orientation.LEFT_RIGHT
            45 -> GradientDrawable.Orientation.BL_TR
            90 -> GradientDrawable.Orientation.BOTTOM_TOP
            135 -> GradientDrawable.Orientation.BR_TL
            180 -> GradientDrawable.Orientation.RIGHT_LEFT
            225 -> GradientDrawable.Orientation.TR_BL
            270 -> GradientDrawable.Orientation.TOP_BOTTOM
            315 -> GradientDrawable.Orientation.TL_BR
            else -> {
                GradientDrawable.Orientation.TOP_BOTTOM
            }
        }
        drawable.orientation = orientation

        val centerX = attr.getFloat(R.styleable.BackgroundShape_shape_gradient_centerX, 0.5f)
        val centerY = attr.getFloat(R.styleable.BackgroundShape_shape_gradient_centerY, 0.5f)
        drawable.setGradientCenter(centerX, centerY)

        val colors = mutableListOf<Int>()
        if (attr.hasValue(R.styleable.BackgroundShape_shape_gradient_startColor)) {
            colors.add(attr.getColor(R.styleable.BackgroundShape_shape_gradient_startColor, 0))
        }

        if (attr.hasValue(R.styleable.BackgroundShape_shape_gradient_centerColor)) {
            colors.add(
                attr.getColor(
                    R.styleable.BackgroundShape_shape_gradient_centerColor,
                    0
                )
            )
        }

        if (attr.hasValue(R.styleable.BackgroundShape_shape_gradient_endColor)) {
            colors.add(attr.getColor(R.styleable.BackgroundShape_shape_gradient_endColor, 0))
        }

        drawable.colors = colors.toIntArray()

        val gradientRadius =
            attr.getDimension(
                R.styleable.BackgroundShape_shape_gradient_gradientRadius,
                0f
            )
        drawable.gradientRadius = gradientRadius
    }

    private fun setPadding(drawable: GradientDrawable, attr: TypedArray) {
        val padding = Rect()
        padding.left =
            attr.getDimension(R.styleable.BackgroundShape_shape_padding_left, 0f).toInt()
        padding.top =
            attr.getDimension(R.styleable.BackgroundShape_shape_padding_top, 0f).toInt()
        padding.right =
            attr.getDimension(R.styleable.BackgroundShape_shape_padding_right, 0f).toInt()
        padding.bottom =
            attr.getDimension(R.styleable.BackgroundShape_shape_padding_bottom, 0f).toInt()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            drawable.setPadding(padding.left, padding.top, padding.right, padding.bottom)
        } else {
            try {
                val paddingField: Field = drawable.javaClass.getDeclaredField("mPadding")
                paddingField.isAccessible = true
                paddingField.set(drawable, padding)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setSolid(drawable: GradientDrawable, attr: TypedArray) {
        val solidColor = attr.getColor(R.styleable.BackgroundShape_shape_solid_color, 0)
        drawable.setColor(solidColor)
    }

    private fun setSize(drawable: GradientDrawable, attr: TypedArray) {
        val width =
            attr.getDimension(R.styleable.BackgroundShape_shape_size_width, -1f)
        val height =
            attr.getDimension(R.styleable.BackgroundShape_shape_size_width, -1f)
        drawable.setSize(width.toInt(), height.toInt())
    }

    private fun setStroke(drawable: GradientDrawable, attr: TypedArray) {
        val strokeWidth =
            attr.getDimension(R.styleable.BackgroundShape_shape_stroke_width, 0f)
        val strokeColor =
            attr.getColor(R.styleable.BackgroundShape_shape_stroke_color, 0)
        val dashWidth =
            attr.getDimension(R.styleable.BackgroundShape_shape_stroke_dashWidth, 0f)
        val dashGap =
            attr.getDimension(R.styleable.BackgroundShape_shape_stroke_dashWidth, 0f)
        drawable.setStroke(strokeWidth.toInt(), strokeColor, dashWidth, dashGap)
    }
}