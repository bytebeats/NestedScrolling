package me.bytebeats.views.nestedscrolling

import android.view.View

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created on 2021/9/7 20:46
 * @Version 1.0
 * @Description 嵌套滚动父 View 帮助类
 */

class ParentViewHelper(private val parentView: View) : View.OnLayoutChangeListener {

    fun addChildView(iChildView: IChildView) {

    }
    fun removeChildView(iChildView: IChildView) {

    }


    override fun onLayoutChange(
        v: View?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {

    }
}