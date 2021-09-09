package me.bytebeats.views.nestedscrolling.refresh

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created on 2021/9/8 21:04
 * @Version 1.0
 * @Description TO-DO
 */

abstract class AbstractRefreshHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), IRefreshHeader {

    init {
        val frameLayoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        frameLayoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams = frameLayoutParams
    }

    override fun setHeight(height: Int) {
        layoutParams.height = height
        requestLayout()
    }

    override fun setMargins(left: Int, top: Int, right: Int, bottom: Int) {
        (layoutParams as LayoutParams).setMargins(left, top, right, bottom)
        requestLayout()
    }

    companion object {
        fun parseRefreshHeader(context: Context, attrs: AttributeSet?, name: String?): AbstractRefreshHeader? {
            return if (name.isNullOrEmpty()) null else try {
                val clazz = Class.forName(name, true, context.classLoader) as Class<AbstractRefreshHeader>
                val constructor = clazz.getConstructor(Context::class.java, AttributeSet::class.java)
                constructor.isAccessible = true
                constructor.newInstance(context, attrs)
            } catch (ignore: ClassNotFoundException) {
                throw RuntimeException("Could not inflate RefreshHeaderLayout subclass $name", ignore)
            }
        }
    }
}