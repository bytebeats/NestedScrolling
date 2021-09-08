package me.bytebeats.views.nestedscrolling.redirect

import android.view.View
import androidx.core.view.NestedScrollingChild3
import androidx.core.view.ViewCompat
import me.bytebeats.views.nestedscrolling.IChildView

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created on 2021/9/8 10:20
 * @Version 1.0
 * @Description TO-DO
 */

abstract class ScrollRedirectToChild : NestedScrollingChild3 {
    abstract val iChildView: IChildView
    private val childView: View
        get() = iChildView.childView()

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        ViewCompat.setNestedScrollingEnabled(childView, enabled)
    }

    override fun isNestedScrollingEnabled(): Boolean = ViewCompat.isNestedScrollingEnabled(childView)

    override fun startNestedScroll(axes: Int, type: Int): Boolean = ViewCompat.startNestedScroll(childView, axes, type)

    override fun startNestedScroll(axes: Int): Boolean = ViewCompat.startNestedScroll(childView, axes)

    override fun stopNestedScroll(type: Int) {
        ViewCompat.stopNestedScroll(childView, type)
    }

    override fun stopNestedScroll() {
        ViewCompat.stopNestedScroll(childView)
    }

    override fun hasNestedScrollingParent(type: Int): Boolean = ViewCompat.hasNestedScrollingParent(childView, type)

    override fun hasNestedScrollingParent(): Boolean = ViewCompat.hasNestedScrollingParent(childView)

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int,
        consumed: IntArray
    ) {
        ViewCompat.dispatchNestedScroll(
            childView,
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow,
            type,
            consumed
        )
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean = ViewCompat.dispatchNestedScroll(
        childView,
        dxConsumed,
        dyConsumed,
        dxUnconsumed,
        dyUnconsumed,
        offsetInWindow,
        type
    )

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?
    ): Boolean =
        ViewCompat.dispatchNestedScroll(childView, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow)

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean = ViewCompat.dispatchNestedPreScroll(childView, dx, dy, consumed, offsetInWindow, type)

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean =
        ViewCompat.dispatchNestedPreScroll(childView, dx, dy, consumed, offsetInWindow)

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean =
        ViewCompat.dispatchNestedFling(childView, velocityX, velocityY, consumed)

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean =
        ViewCompat.dispatchNestedPreFling(childView, velocityX, velocityY)
}