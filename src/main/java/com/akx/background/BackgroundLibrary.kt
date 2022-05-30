package com.akx.background

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import androidx.core.view.LayoutInflaterCompat

object BackgroundLibrary {
    /**
     * 在 Activity onCreate内，setContentView 之前食用
     */
    fun inject(context: Context): LayoutInflater? {
        val inflater: LayoutInflater? = if (context is Activity) {
            context.layoutInflater
        } else {
            LayoutInflater.from(context)
        }
        if (inflater == null) {
            return null
        }
        forceSetFactory2(inflater)
        return inflater
    }

    private fun forceSetFactory2(inflater: LayoutInflater) {
        val compatClass = LayoutInflaterCompat::class.java
        val inflaterClass = LayoutInflater::class.java
        try {
            val sCheckedField = compatClass.getDeclaredField("sCheckedField")
            sCheckedField.isAccessible = true
            sCheckedField.setBoolean(compatClass, false)
            val mFactory = inflaterClass.getDeclaredField("mFactory")
            mFactory.isAccessible = true
            val mFactory2 = inflaterClass.getDeclaredField("mFactory2")
            mFactory2.isAccessible = true
            val factory = BackgroundFactory()
            if (inflater.factory2 != null) {
                factory.setInterceptFactory2(inflater.factory2)
            } else if (inflater.factory != null) {
                factory.setInterceptFactory(inflater.factory)
            }
            mFactory2[inflater] = factory
            mFactory[inflater] = factory
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }
    }
}