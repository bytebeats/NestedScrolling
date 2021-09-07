package me.bytebeats.views.nestedscrolling

import android.view.View
import android.view.ViewGroup

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created on 2021/9/7 20:45
 * @Version 1.0
 * @Description 嵌套滚动时 对应的子View
 */

interface IChildView {
    val mChildViewHelper: ChildViewHelper

    fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)

    fun iParentView(): IParentView?

    fun onParentViewScrolling(scrollY: Int)

    fun childView(): View

    companion object {
        fun measureSpecArray(
            widthMeasureSpec: Int,
            height: Int,
            childViewHelper: ChildViewHelper
        ): IntArray {
            if (height == ViewGroup.LayoutParams.WRAP_CONTENT || height > 0 || childViewHelper.iParentView() !is View) {
                return intArrayOf()
            }
            val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                (childViewHelper.iParentView() as View).measuredHeight,
                if (height == ViewGroup.LayoutParams.MATCH_PARENT) View.MeasureSpec.EXACTLY else View.MeasureSpec.AT_MOST
            )
            return intArrayOf(widthMeasureSpec, heightMeasureSpec)
        }
    }
}