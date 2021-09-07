package me.bytebeats.views.nestedscrolling

import androidx.core.view.NestedScrollingParent3

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created on 2021/9/7 20:47
 * @Version 1.0
 * @Description 嵌套滚动时对应的父View
 */

interface IParentView : NestedScrollingParent3 {
    val mParentViewHelper: ParentViewHelper
    fun addChildView(iChildView: IChildView)
    fun removeChildView(iChildView: IChildView)
    fun fixInconsistentChildScroll()
}