package me.bytebeats.views.nestedscrolling

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import me.bytebeats.views.nestedscrolling.redirect.IScrollRedirect
import me.bytebeats.views.nestedscrolling.redirect.ScrollRedirect

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created on 2021/9/7 21:27
 * @Version 1.0
 * @Description TO-DO
 */

class ChildView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), IChildView, IScrollRedirect by ScrollRedirect() {
    override val mChildViewHelper: ChildViewHelper
        get() = ChildViewHelper(this)

    init {
        init(this)
    }

    override fun iParentView(): IParentView? = mChildViewHelper.iParentView()

    override fun onParentViewScrolling(scrollY: Int) {
    }

    override fun childView(): View = getChildAt(0)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measureSpecs = IChildView.measureSpecArray(widthMeasureSpec, layoutParams.height, mChildViewHelper)
        if (measureSpecs.size == 2) {
            setMeasuredDimension(measureSpecs[0], measureSpecs[1])
            super.onMeasure(measureSpecs[0], measureSpecs[1])
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }
}