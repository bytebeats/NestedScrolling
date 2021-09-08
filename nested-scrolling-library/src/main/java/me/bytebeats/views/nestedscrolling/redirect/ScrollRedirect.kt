package me.bytebeats.views.nestedscrolling.redirect

import android.view.View
import android.view.ViewGroup
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import me.bytebeats.views.nestedscrolling.IChildView

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created on 2021/9/8 10:30
 * @Version 1.0
 * @Description TO-DO
 */

class ScrollRedirect : ScrollRedirectToChild(), IScrollRedirect, View.OnLayoutChangeListener {
    private lateinit var parentHelper: NestedScrollingParentHelper
    private lateinit var childHelper: NestedScrollingChildHelper
    override lateinit var iChildView: IChildView

    private val onLayoutChangeListeners = mutableListOf<View.OnLayoutChangeListener>()

    override fun init(iChildView: IChildView) {
        this.iChildView = iChildView
        parentHelper = NestedScrollingParentHelper(iChildView as ViewGroup)
        childHelper = NestedScrollingChildHelper(iChildView as ViewGroup)
        childHelper.isNestedScrollingEnabled = true
    }

    override fun scrollBy(dx: Int, dy: Int) {
        iChildView.childView().scrollBy(dx, dy)
    }

    override fun canScrollVertically(direction: Int): Boolean = iChildView.childView().canScrollVertically(direction)

    override fun addOnLayoutChangeListener(listener: View.OnLayoutChangeListener?) {
        if (onLayoutChangeListeners.isEmpty()) {
            iChildView.childView().addOnLayoutChangeListener(this)
        }
        listener?.let {
            if (!onLayoutChangeListeners.contains(it)) {
                onLayoutChangeListeners.add(it)
            }
        }
    }

    override fun removeOnLayoutChangeListener(listener: View.OnLayoutChangeListener?) {
        listener?.let {
            if (onLayoutChangeListeners.contains(it)) {
                onLayoutChangeListeners.remove(it)
            }
        }
        if (onLayoutChangeListeners.isEmpty() && listener != null) {
            iChildView.childView().removeOnLayoutChangeListener(listener)
        }
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
        for (listener in onLayoutChangeListeners) {
            listener.onLayoutChange(iChildView as View, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom)
        }
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return iChildView.childView() == target && (axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0)
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int): Boolean {
        return onStartNestedScroll(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH)
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        parentHelper.onNestedScrollAccepted(child, target, axes, type)
        childHelper.startNestedScroll(axes, type)
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        onNestedScrollAccepted(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        childHelper.dispatchNestedPreScroll(dx, dy, consumed, null, type)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        onNestedPreScroll(target, dx, dy, consumed, ViewCompat.TYPE_TOUCH)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, null, type, consumed)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, IntArray(2))
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, ViewCompat.TYPE_TOUCH, IntArray(2))
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean =
        childHelper.dispatchNestedPreFling(velocityX, velocityY)

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean =
        childHelper.dispatchNestedFling(velocityX, velocityY, consumed)

    override fun onStopNestedScroll(target: View, type: Int) {
        parentHelper.onStopNestedScroll(target, type)
        childHelper.stopNestedScroll(type)
    }

    override fun onStopNestedScroll(target: View) {
        onStopNestedScroll(target, ViewCompat.TYPE_TOUCH)
    }

    override fun getNestedScrollAxes(): Int = parentHelper.nestedScrollAxes
}