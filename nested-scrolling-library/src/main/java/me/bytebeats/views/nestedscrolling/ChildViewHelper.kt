package me.bytebeats.views.nestedscrolling

import android.view.View
import android.view.ViewGroup

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created on 2021/9/7 20:46
 * @Version 1.0
 * @Description 嵌套滚动子 View 帮助类
 */

class ChildViewHelper(private val iChildView: IChildView) : View.OnAttachStateChangeListener {
    private var iParentView: IParentView? = null
    private var isParentViewMadeSure = false
    private val childView
        get() = iChildView as ViewGroup

    init {
        childView.addOnAttachStateChangeListener(this)
        if (childView.isAttachedToWindow) {
            onViewAttachedToWindow(childView)
        }
    }

    override fun onViewAttachedToWindow(v: View?) {
        iParentView()?.addChildView(iChildView)
    }

    override fun onViewDetachedFromWindow(v: View?) {
        iParentView()?.removeChildView(iChildView)
    }

    private fun makeSureParentView() {
        if (!isParentViewMadeSure) {
            var parent = childView.parent
            while (parent != null && parent !is IParentView) {
                parent = parent.parent
            }
            iParentView = parent as IParentView?
            isParentViewMadeSure = true
        }
    }

    fun iParentView(): IParentView? {
        makeSureParentView()
        return iParentView
    }
}