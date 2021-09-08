package me.bytebeats.views.nestedscrolling.redirect

import android.view.View
import androidx.core.view.NestedScrollingChild3
import androidx.core.view.NestedScrollingParent3
import me.bytebeats.views.nestedscrolling.IChildView

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created on 2021/9/8 10:11
 * @Version 1.0
 * @Description TO-DO
 */

interface IScrollRedirect : NestedScrollingParent3, NestedScrollingChild3 {
    fun init(iChildView: IChildView)
    fun scrollBy(dx: Int, dy: Int)
    fun canScrollVertically(direction: Int): Boolean
    fun addOnLayoutChangeListener(listener: View.OnLayoutChangeListener?)
    fun removeOnLayoutChangeListener(listener: View.OnLayoutChangeListener?)
}