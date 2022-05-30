package com.akx.background

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.collection.ArrayMap
import java.lang.reflect.Constructor

class BackgroundFactory : LayoutInflater.Factory2 {

    private var mViewCreateFactory: LayoutInflater.Factory? = null
    private var mViewCreateFactory2: LayoutInflater.Factory2? = null
    private val sConstructorSignature = arrayOf(
        Context::class.java,
        AttributeSet::class.java
    )
    private val mConstructorArgs = arrayOfNulls<Any>(2)
    private val sConstructorMap: MutableMap<String, Constructor<out View?>> = ArrayMap()

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {
        return onCreateView(name, context, attrs)
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        var view: View? = null
        // 先用其他库的 Factory 创建 View。
        if (mViewCreateFactory2 != null) {
            view = mViewCreateFactory2!!.onCreateView(name, context, attrs)
            if (view == null) {
                view = mViewCreateFactory2!!.onCreateView(null, name, context, attrs)
            }
        } else if (mViewCreateFactory != null) {
            view = mViewCreateFactory!!.onCreateView(name, context, attrs)
        }

        var fView = view
        //不管其他库有没有创建 View。先检查有没有包含指定的 attr
        //如果 attrs 里没有 Background 自定义的属性。表示不需要构建背景，交给命运

        //自定义 Shape 背景
        if (checkHasAttr(context, attrs, Shape.styleableId)) {
            //如果其他 Factory 没有创建View。我们自己创建
            fView = checkCreateView(fView, context, name, attrs)

            val shapeAttr = context.obtainStyledAttributes(attrs, Shape.styleableId)
            val bg = Shape(context, shapeAttr).createDrawable()

            fView?.background = bg

            shapeAttr.recycle()
        }

        //自定义 Selector 背景，支持颜色，Drawable图片，自定义Shape
        if (checkHasAttr(context, attrs, Selector.styleableId)) {
            fView = checkCreateView(fView, context, name, attrs)

            val selectorAttr = context.obtainStyledAttributes(attrs, Selector.styleableId)
            val bg = Selector(context, selectorAttr).createDrawable()

            fView?.background = bg

            selectorAttr.recycle()
        }

        return fView
    }

    /**
     * 检查是否包含指定的自定义属性集
     */
    fun checkHasAttr(
        context: Context,
        attributeSet: AttributeSet,
        styleableRes: IntArray
    ): Boolean {
        val attr = context.obtainStyledAttributes(attributeSet, styleableRes)
        return attr.indexCount > 0
    }

    private fun checkCreateView(
        view: View?,
        context: Context,
        name: String,
        attrs: AttributeSet
    ): View? {
        var fView = view
        if (fView == null) {
            fView = createViewFromTag(context, name, attrs)
        }
        return fView
    }

    /**
     * 通过标签创建 View
     */
    private fun createViewFromTag(context: Context, name: String?, attrs: AttributeSet): View? {
        var fName = name
        if (fName.isNullOrEmpty()) {
            return null
        }

        if (fName == "view") {
            fName = attrs.getAttributeValue(null, "class")
        }

        if (fName == null) {
            return null
        }

        return try {
            mConstructorArgs[0] = context
            mConstructorArgs[1] = attrs
            if (-1 == fName.indexOf('.')) {
                var view: View? = null
                if ("View" == fName) {
                    view = createView(context, fName, "android.view.")
                }
                if (view == null) {
                    view = createView(context, fName, "android.widget.")
                }
                if (view == null) {
                    view = createView(context, fName, "android.webkit.")
                }
                view
            } else {
                createView(context, fName, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            //Log.w("BackgroundLibrary", "cannot create 【$fName】 : ")
            null
        } finally {
            mConstructorArgs[0] = null
            mConstructorArgs[1] = null
        }
    }

    /**
     * 内部创建 View。（仿 AppCompatActivity 中 AppCompatDelegate 的实现）
     */
    private fun createView(context: Context, name: String, prefix: String?): View? {
        var constructor = sConstructorMap[name]
        return try {
            if (constructor == null) {
                val clazz = context.classLoader.loadClass(
                    if (prefix != null) prefix + name else name
                ).asSubclass(View::class.java)
                constructor = clazz.getConstructor(*sConstructorSignature)
                sConstructorMap[name] = constructor
            }
            constructor!!.isAccessible = true
            constructor.newInstance(*mConstructorArgs)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 设置兼容其他的 Factory
     */
    fun setInterceptFactory(factory: LayoutInflater.Factory) {
        mViewCreateFactory = factory
    }

    /**
     * 设置兼容其他的 Factory2
     */
    fun setInterceptFactory2(factory: LayoutInflater.Factory2) {
        mViewCreateFactory2 = factory
    }

}