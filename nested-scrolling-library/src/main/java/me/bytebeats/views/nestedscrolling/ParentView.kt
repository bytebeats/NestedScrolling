package me.bytebeats.views.nestedscrolling

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import kotlin.math.absoluteValue
import kotlin.math.sign

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created on 2021/9/7 21:35
 * @Version 1.0
 * @Description TO-DO
 */

class ParentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr), IParentView, IChildView {

    private var notifiedChild: IChildView? = null

    override val mParentViewHelper: ParentViewHelper
        get() = ParentViewHelper(this)

    override fun addChildView(iChildView: IChildView) {
        mParentViewHelper.addChildView(iChildView)
    }

    override fun removeChildView(iChildView: IChildView) {
        mParentViewHelper.removeChildView(iChildView)
    }

    override fun fixInconsistentChildScroll() {
        val (child, top) = mParentViewHelper.topChild(scrollY)
        if (child == null) return
        val diff = scrollY - top
        if (diff <= 0 || !child.canScrollVertically(diff)) return
        child.scrollBy(0, Int.MAX_VALUE)
    }

    override val mChildViewHelper: ChildViewHelper
        get() = ChildViewHelper(this)

    override fun iParentView(): IParentView? = mChildViewHelper.iParentView()

    override fun onParentViewScrolling(scrollY: Int) {
    }

    override fun childView(): View = this

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        if (dy == 0) return false
        val consumedArray = consumed ?: IntArray(2)
        var isConsumed = super.dispatchNestedPreScroll(dx, dy, consumedArray, offsetInWindow, type)
        var deltaY = dy
        if (isConsumed) {
            deltaY -= consumedArray[1]
        }
        if (deltaY == 0) return isConsumed
        isConsumed = dispatchScroll(deltaY, false)
        if (isConsumed) {
            consumedArray[1] = dy
        }
        return isConsumed
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
        if (target !is IChildView || target.iParentView() != this) {
            super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed)
            return
        }
        if (dyUnconsumed == 0) return
        if (dispatchScroll(dyUnconsumed, true)) {
            consumed[1] = dyUnconsumed
        } else {
            super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed)
        }
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        if (target !is IChildView || target.iParentView() != this) {
            return super.onNestedPreFling(target, velocityX, velocityY)
        }

        fling(velocityY.toInt())
        return true
    }

    override fun scrollBy(x: Int, y: Int) {
        dispatchScroll(y, false)
    }

    private fun dispatchScroll(dy: Int, fromChild: Boolean): Boolean {
        if (dy == 0) return false
        val top = scrollY
        val (child, childTop) = mParentViewHelper.closestChild(top, dy < 0)
        if (child != null) {
            if (top == childTop) {
                if (!child.canScrollVertically(dy)) {
                    if (superCanScrollVertically(dy)) {
                        superScrollBy(0, dy.sign)
                        dispatchScroll(dy - dy.sign, false)
                        return true
                    }
                    return false
                }
                scrollChildVertically(child, dy, fromChild)
                return true
            } else if (dy.absoluteValue > (childTop - top).absoluteValue) {
                val dy2 = childTop - top
                if (!superCanScrollVertically(dy2)) return false
                superScrollBy(0, dy2)
                scrollChildVertically(child, dy - dy2, fromChild)
                return true
            }
        }
        if (superCanScrollVertically(dy)) {
            superScrollBy(0, dy)
            return true
        }
        return false
    }

    private fun superCanScrollVertically(direction: Int): Boolean = super.canScrollVertically(direction)

    private fun superScrollBy(dx: Int, dy: Int) {
        super.scrollBy(dx, dy)
        notifyParentScrollChange()
    }

    private fun notifyParentScrollChange() {
        val (child, top) = mParentViewHelper.topChild(scrollY)
        if (child == null) return
        val diff = scrollY - top
        (child as IChildView).onParentViewScrolling(diff)
        if (notifiedChild != child) {
            notifiedChild?.onParentViewScrolling(0)
            notifiedChild = child
        }
    }

    private fun scrollChildVertically(child: View, dy: Int, fromChild: Boolean) {
        if (!fromChild) {
            ViewCompat.startNestedScroll(child, ViewCompat.SCROLL_AXIS_VERTICAL)
        }
        child.scrollBy(0, dy)
        if (!fromChild) {
            ViewCompat.startNestedScroll(child, ViewCompat.SCROLL_AXIS_VERTICAL)
        }
    }

    override fun canScrollVertically(direction: Int): Boolean {
        if (superCanScrollVertically(direction)) {
            return true
        } else {
            val top = scrollY
            val (child, childTop) = mParentViewHelper.closestChild(top, direction < 0)
            if (child != null && childTop != top) {
                return child.canScrollVertically(direction)
            }
            return false
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        if (heightSpecMode == MeasureSpec.AT_MOST) {
            val height = MeasureSpec.makeMeasureSpec(heightSpecSize, MeasureSpec.EXACTLY)
            setMeasuredDimension(widthMeasureSpec, height)
            super.onMeasure(widthMeasureSpec, height)
            return
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}