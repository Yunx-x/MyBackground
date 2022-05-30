package com.akx.background

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable

abstract class IBackgroundCreator(val context: Context, val  attr: TypedArray) {

    abstract fun createDrawable(): Drawable?
}