package com.akx.background

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable

class Selector(context: Context, attr: TypedArray) :
    IBackgroundCreator(context, attr) {

    companion object {
        val styleableId = R.styleable.BackgroundSelector
    }

    private val stateMap = mapOf(
        Pair(
            R.styleable.BackgroundSelector_state_pressed,
            android.R.attr.state_pressed
        ),
        Pair(
            R.styleable.BackgroundSelector_state_unpressed,
            -android.R.attr.state_pressed
        ),
        Pair(
            R.styleable.BackgroundSelector_state_focused,
            android.R.attr.state_focused
        ),
        Pair(
            R.styleable.BackgroundSelector_state_unfocused,
            -android.R.attr.state_focused
        ),
        Pair(
            R.styleable.BackgroundSelector_state_selected,
            android.R.attr.state_selected
        ),
        Pair(
            R.styleable.BackgroundSelector_state_unselected,
            -android.R.attr.state_selected
        ),
        Pair(
            R.styleable.BackgroundSelector_state_checkable,
            android.R.attr.state_checkable
        ),
        Pair(
            R.styleable.BackgroundSelector_state_uncheckable,
            -android.R.attr.state_checkable
        ),
        Pair(
            R.styleable.BackgroundSelector_state_checked,
            android.R.attr.state_checked
        ),
        Pair(
            R.styleable.BackgroundSelector_state_unchecked,
            -android.R.attr.state_checked
        ),
        Pair(
            R.styleable.BackgroundSelector_state_enabled,
            android.R.attr.state_enabled
        ),
        Pair(
            R.styleable.BackgroundSelector_state_unenabled,
            -android.R.attr.state_enabled
        ),
        Pair(
            R.styleable.BackgroundSelector_state_window_focused,
            android.R.attr.state_window_focused
        ),
        Pair(
            R.styleable.BackgroundSelector_state_window_unfocused,
            -android.R.attr.state_window_focused
        )
    )

    override fun createDrawable(): Drawable? {
        val hasOrNo = attr.indexCount > 0
        return if (hasOrNo) {
            val drawable = StateListDrawable()
            for (i in 0 until attr.indexCount) {
                val attrIndex = attr.getIndex(i)
                val attrType = attr.getType(attrIndex)
                var stateDrawable: Drawable? = null
                when (attrType) {
                    28, 29 -> {//Color
                        val color = attr.getColor(attrIndex, 0)
                        if (color != 0) {
                            stateDrawable = GradientDrawable()
                            stateDrawable.setColor(color)
                        }
                    }
                    3 -> {//Drawable
                        stateDrawable = attr.getDrawable(attrIndex)
                    }
                    1 -> {//Custom Shape
                        val styleId = attr.getResourceId(attrIndex, 0)
                        val styleAttr = if (styleId == 0) {
                            null
                        } else {
                            context.obtainStyledAttributes(styleId, Shape.styleableId)
                        }

                        stateDrawable = if (styleAttr == null) {
                            GradientDrawable()
                        } else {
                            Shape(context, styleAttr).createDrawable()
                        }

                        styleAttr?.recycle()
                    }
                    else -> {
                        stateDrawable = null
                    }
                }
                stateDrawable = stateDrawable ?: GradientDrawable()
                drawable.addState(intArrayOf(getStateId(attrIndex)), stateDrawable)
            }
            drawable
        } else {
            null
        }
    }

    private fun getStateId(attrIndex: Int): Int {
        return if (stateMap.containsKey(attrIndex)) {
            stateMap[attrIndex]!!
        } else {
            0
        }
    }
}