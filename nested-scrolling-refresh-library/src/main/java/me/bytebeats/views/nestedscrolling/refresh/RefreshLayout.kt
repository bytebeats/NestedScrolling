package me.bytebeats.views.nestedscrolling.refresh

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.AbsListView
import android.widget.FrameLayout
import android.widget.ListView
import androidx.core.view.*
import androidx.core.widget.ListViewCompat
import java.lang.IllegalArgumentException
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created on 2021/9/9 10:46
 * @Version 1.0
 * @Description TO-DO
 */

class RefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var isRefreshing = false
    private var mState = State.NONE
    private var mTotalUnconsumed = 0F
    private val nestedScrollingParentHelper by lazy { NestedScrollingParentHelper(this) }
    private val nestedScrollingChildHelper by lazy { NestedScrollingChildHelper(this) }
    private val parentScrollConsumed by lazy { IntArray(2) }
    private val parentOffsetInWindow by lazy { IntArray(2) }
    private var isNestedScrollInProgress = false

    private var isBeingDragged = false
    private var activePointerId = INVALID_POINTER

    private val touchSlog by lazy { ViewConfiguration.get(context).scaledTouchSlop }
    private var initialMotionY = 0F
    private var initialDownY = 0F

    private var refreshingDistance = 0

    private var refreshableView: View? = null
    private var refreshHeader: AbstractRefreshHeader? = null
    private var headerHeight = -1

    private var curSmoothScrollCmd: SmoothScrollCommand? = null
    private val scrollAnimationInterpolator by lazy { DecelerateInterpolator() }

    var onRefreshListener: OnRefreshListener? = null
    var onChildScrollUpCallback: OnChildScrollUpCallback? = null
    private val onSmoothScrollFinishedListener by lazy {
        object : OnSmoothScrollFinishedListener {
            override fun onSmoothScrollFinished() {
                tryInvokeRefreshListener()
            }
        }
    }

    var maxDistanceToPull: Int = -1
    var scrollableWhenRefreshing: Boolean = true
    var refreshHeaderPath: String? = null

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.RefreshLayout)
        scrollableWhenRefreshing = a.getBoolean(R.styleable.RefreshLayout_scrollableWhenRefreshing, true)
        maxDistanceToPull = a.getDimensionPixelOffset(R.styleable.RefreshLayout_maxDistanceToPull, 60)
        refreshHeaderPath = a.getString(R.styleable.RefreshLayout_refreshHeaderPath)
        if (refreshHeaderPath.isNullOrEmpty()) {
            refreshHeader = AbstractRefreshHeader.parseRefreshHeader(context, attrs, refreshHeaderPath)
                ?: throw IllegalArgumentException("$refreshHeaderPath has to be ${AbstractRefreshHeader::class.java}")
        } else {
            throw RuntimeException("attribute refreshHeaderPath has to been aligned.")
        }
        a.recycle()
        isNestedScrollingEnabled = true
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val flp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        flp.gravity = Gravity.TOP or Gravity.START
        layoutParams = flp
        if (refreshableView == null) {
            makeSureTarget()
            refreshableView?.let {
                ViewCompat.setNestedScrollingEnabled(it, true)
            }
        }
        refreshHeader?.let {
            it.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            headerHeight = it.measuredHeight
            refreshingDistance = headerHeight
            val headerLayoutParam = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            headerLayoutParam.topMargin = -headerHeight
            addView(refreshHeader, -1, headerLayoutParam)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        makeSureTarget()
        if (ev == null || isNestedScrollInProgress) return false
        if (mState == State.SCROLL_TO_BACK || mState == State.SCROLL_TO_REFRESH) {
            return true
        }
        if (!isEnabled || canChildScrollUp() || isNestedScrollInProgress) {
            return false
        }
        val action = ev.actionMasked
        var pointerIdx = 0
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                activePointerId = ev.getPointerId(0)
                isBeingDragged = false
                pointerIdx = ev.findPointerIndex(activePointerId)
                if (pointerIdx < 0) {
                    return false
                }
                initialDownY = ev.getY(pointerIdx)
                if (mState == State.REFRESHING) {
                    initialDownY -= refreshingDistance / DRAG_RATE
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (activePointerId == INVALID_POINTER) {
                    return false
                }
                pointerIdx = ev.findPointerIndex(activePointerId)
                if (pointerIdx < 0) {
                    return false
                }
                val y = ev.getY(pointerIdx)
                if (mState == State.REFRESHING) {
                    val dy = y - (initialDownY + refreshingDistance / DRAG_RATE)
                    if (dy.absoluteValue > touchSlog && !isBeingDragged) {
                        if (dy > 0) {
                            initialMotionY = initialDownY + touchSlog
                        } else {
                            initialMotionY = initialDownY - touchSlog
                        }
                        isBeingDragged = true
                    }
                } else {
                    startDragging(y - initialDownY)
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(ev)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isBeingDragged = false
                activePointerId = INVALID_POINTER
                if (mState != State.REFRESHING && scrollY != 0) {
                    reset()
                }
            }
        }
        return isBeingDragged
    }

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        if (Build.VERSION.SDK_INT < 21 && refreshableView is AbsListView
            || refreshableView != null && !ViewCompat.isNestedScrollingEnabled(refreshableView!!)) {
            // do nothing here
        } else {
            super.requestDisallowInterceptTouchEvent(disallowIntercept)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null || isNestedScrollInProgress) {
            return false
        }
        var pointerIdx = -1
        if (mState == State.SCROLL_TO_BACK || mState == State.SCROLL_TO_REFRESH) {
            activePointerId = event.getPointerId(0)
            isBeingDragged = false
            pointerIdx = event.findPointerIndex(activePointerId)
            if (pointerIdx < 0) {
                return false
            }
            initialDownY = event.getY(pointerIdx)
            curSmoothScrollCmd?.stop()
            mState = State.MANUAL_SCROLLING
            initialDownY += scrollY / DRAG_RATE - touchSlog
            initialMotionY = initialDownY
            return true
        }
        if (mState == State.REFRESHING) {
            isBeingDragged = true
        } else if (!isEnabled || canChildScrollUp() || isNestedScrollInProgress) {
            return false
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activePointerId = event.getPointerId(0)
                isBeingDragged = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (activePointerId == INVALID_POINTER) {
                    return false
                }
                pointerIdx = event.findPointerIndex(activePointerId)
                if (pointerIdx < 0) {
                    return false
                }
                val y = event.getY(pointerIdx)
                val dy = y - initialDownY
                startDragging(dy)

                val overScrollTop = (initialMotionY - y) * DRAG_RATE
                if (isBeingDragged) {
                    if (overScrollTop <= 0) {
                        moveHeader(overScrollTop)
                    } else {
                        curSmoothScrollCmd?.stop()
                        isBeingDragged = false
                        setBodyScroll(0)
                        return false
                    }
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                pointerIdx = event.actionIndex
                if (pointerIdx < 0) {
                    return false
                }
                val secondPointerY = event.getY(pointerIdx)
                val secondPointerId = event.getPointerId(pointerIdx)
                initialDownY = secondPointerY + scrollY / DRAG_RATE - touchSlog
                activePointerId = secondPointerId
                isBeingDragged = false
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(event)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                pointerIdx = event.findPointerIndex(activePointerId)
                if (pointerIdx < 0) {
                    reset()
                    return false
                }
                if (isBeingDragged) {
                    val y = event.getY(pointerIdx)
                    val overScrollTop = (initialMotionY - y) * DRAG_RATE
                    isBeingDragged = false
                    finishHeader(overScrollTop)
                }
                activePointerId = INVALID_POINTER
                return false
            }
        }
        return true
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (!enabled) {
            reset()
        }
    }

    override fun onStartNestedScroll(child: View?, target: View?, nestedScrollAxes: Int): Boolean {
        val result = isEnabled && (nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0)
        if (result) {
            if (refreshableView?.canScrollVertically(1) != true) {
                return false
            }
        }
        return result
    }

    override fun onNestedScrollAccepted(child: View?, target: View?, axes: Int) {
        if (child != null && target != null) {
            nestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes)
        }
        startNestedScroll(axes and ViewCompat.SCROLL_AXIS_VERTICAL)
        mTotalUnconsumed = if (mState == State.REFRESHING) -refreshingDistance / DRAG_RATE else 0F
        isNestedScrollInProgress = true
    }

    override fun onNestedPreScroll(target: View?, dx: Int, dy: Int, consumed: IntArray?) {
        if (dy > 0) {
            if (mTotalUnconsumed > 0) {
                if (dy > mTotalUnconsumed) {
                    consumed?.set(1, dy - mTotalUnconsumed.toInt())
                    mTotalUnconsumed = 0F
                } else {
                    mTotalUnconsumed -= dy
                    consumed?.set(1, dy)
                }
            } else if (mTotalUnconsumed < 0) {
                if (dy > mTotalUnconsumed.absoluteValue) {
                    consumed?.set(1, mTotalUnconsumed.toInt() - dy)
                    mTotalUnconsumed = 0F
                } else {
                    mTotalUnconsumed += dy
                    consumed?.set(1, dy)
                }
            }
            moveHeader(mTotalUnconsumed * DRAG_RATE)
        }

        if (consumed != null && dispatchNestedPreScroll(
                dx - consumed[0],
                dy - consumed[1],
                parentScrollConsumed,
                null
            )) {
            consumed[0] += parentScrollConsumed[0]
            consumed[1] += parentScrollConsumed[1]
        }
    }

    override fun getNestedScrollAxes(): Int = nestedScrollingParentHelper.nestedScrollAxes

    override fun onStopNestedScroll(child: View?) {
        if (child != null) {
            nestedScrollingParentHelper.onStopNestedScroll(child)
        }
        isNestedScrollInProgress = false

        if (mTotalUnconsumed != 0F && mState != State.REFRESHING) {
            finishHeader(mTotalUnconsumed * DRAG_RATE)
            mTotalUnconsumed = 0F
        }
        stopNestedScroll()
    }

    override fun onNestedScroll(target: View?, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        // Dispatch up to the nested parent first
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, parentOffsetInWindow)

        // This is a bit of a hack. Nested scrolling works from the bottom up, and as we are
        // sometimes between two nested scrolling views, we need a way to be able to know when any
        // nested scrolling parent has stopped handling events. We do that by using the
        // 'offset in window 'functionality to see if we have been moved from the event.
        // This is a decent indication of whether we should take over the event stream or not.
        val dy = dyUnconsumed + parentOffsetInWindow[1]
        if (!canChildScrollUp()) {
            mTotalUnconsumed += dy
            moveHeader(mTotalUnconsumed * DRAG_RATE)
        }
    }

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        nestedScrollingChildHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean = nestedScrollingChildHelper.isNestedScrollingEnabled

    override fun startNestedScroll(axes: Int): Boolean {
        return nestedScrollingChildHelper.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        nestedScrollingChildHelper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean = nestedScrollingChildHelper.hasNestedScrollingParent()

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?
    ): Boolean {
        return nestedScrollingChildHelper.dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow
        )
    }

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean {
        return nestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun onNestedPreFling(target: View?, velocityX: Float, velocityY: Float): Boolean {
        return dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun onNestedFling(target: View?, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return nestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return nestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    private fun startDragging(scrollY: Float) {
        if (scrollY > touchSlog && !isBeingDragged) {
            isBeingDragged = true
            initialMotionY = initialDownY + touchSlog
        }
    }

    fun refreshAutomatically() {
        setState(State.AUTO_REFRESH)
    }

    fun completeRefresh() {
        isRefreshing = false
        if (mState == State.MANUAL_SCROLLING) {
            return
        }
        refreshHeader?.onRefreshFinish()
        setState(State.SCROLL_TO_BACK)
    }

    private fun makeSureTarget() {
        if (refreshableView == null) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child != refreshHeader) {
                    refreshableView = child
                    break
                }
            }
        }
    }

    private fun onSecondaryPointerUp(event: MotionEvent?) {
        if (event == null) return
        val actionIdx = event.actionIndex
        val pointerId = event.getPointerId(actionIdx)
        if (pointerId == activePointerId) {
            val newActionIdx = if (actionIdx == 0) 1 else 0
            activePointerId = event.getPointerId(newActionIdx)
            val activePointerY = event.getY(newActionIdx)
            initialDownY = activePointerY + scrollY / DRAG_RATE - touchSlog
            isBeingDragged = false
        }
    }

    private fun moveHeader(scrollY: Float) {
        if (scrollY > 0F) return
        if (scrollY == 0F) {
            setBodyScroll(0)
            return
        }
        mState = State.MANUAL_SCROLLING
        setBodyScroll(scrollY.toInt())
        if (scrollY != 0F && !isRefreshing()) {
            refreshHeader?.onPull(-scrollY)
        }
        if (scrollY < refreshingDistance && scrollY > -refreshingDistance) {
            refreshHeader?.alreadyToRefresh(false)
        } else {
            refreshHeader?.alreadyToRefresh(true)
        }
    }

    private fun finishHeader(scrollY: Float) {
        if (scrollY <= -refreshingDistance) {
            setState(State.SCROLL_TO_REFRESH)
        } else {
            setState(State.SCROLL_TO_BACK)
        }
    }

    private fun setState(state: State) {
        when (state) {
            State.SCROLL_TO_BACK -> {
                reset()
            }
            State.SCROLL_TO_REFRESH -> {
                mState = State.SCROLL_TO_REFRESH
                smoothScrollTo(-refreshingDistance, listener = onSmoothScrollFinishedListener)
            }
            State.AUTO_REFRESH -> {
                mState = State.SCROLL_TO_REFRESH
                smoothScrollTo(-refreshingDistance, 350L, 0L, object : OnSmoothScrollFinishedListener {
                    override fun onSmoothScrollFinished() {
                        mState = State.SCROLL_TO_REFRESH
                        postDelayed({ setState(State.SCROLL_TO_REFRESH) }, 150L)
                    }
                })
            }
            State.REFRESHING -> {
                isBeingDragged = false
                mState = State.REFRESHING
            }
        }
    }

    private fun tryInvokeRefreshListener() {
        if (onRefreshListener != null) {
            setState(State.REFRESHING)
            refreshHeader?.onRefreshStart()
            if (!isRefreshing) {
                isRefreshing = true
                onRefreshListener?.onRefresh()
            }
        } else {
            isRefreshing = false
            setState(State.SCROLL_TO_BACK)
        }
    }

    protected fun reset() {
        isBeingDragged = false
        mState = State.SCROLL_TO_BACK
        activePointerId = INVALID_POINTER
        smoothScrollTo(0)
    }

    private fun smoothScrollTo(
        newScrollY: Int,
        duration: Long = SMOOTH_SCROLL_DURATION_MS,
        delayInMillis: Long = 0L,
        listener: OnSmoothScrollFinishedListener? = null
    ) {
        curSmoothScrollCmd?.stop()
        val oldScrollY = scrollY
        if (oldScrollY != newScrollY) {
            curSmoothScrollCmd = SmoothScrollCommand(oldScrollY, newScrollY, duration, listener)
            postDelayed(curSmoothScrollCmd, delayInMillis)
        } else {
            mState = State.NONE
        }
    }

    fun isRefreshing(): Boolean = mState == State.REFRESHING

    private fun canChildScrollUp(): Boolean {
        if (onChildScrollUpCallback != null) {
            return onChildScrollUpCallback!!.canChildScrollUp(this, refreshableView)
        }
        if (refreshableView is ListView) {
            return ListViewCompat.canScrollList(refreshableView as ListView, -1)
        }
        return refreshableView?.canScrollVertically(-1) ?: false
    }

    private fun setBodyScroll(dy: Int) {
        scrollTo(0, dy)
    }

    inner class SmoothScrollCommand(
        private val fromY: Int,
        private val toY: Int,
        private val duration: Long,
        private var listener: OnSmoothScrollFinishedListener? = null
    ) : Runnable {

        private var continueRunning = true
        private var startTime: Long = -1
        private var currentY: Int = -1

        override fun run() {
            if (startTime == -1L) {
                startTime = System.currentTimeMillis()
            } else if (isBeingDragged) {
                return
            } else {
                var normalizedTime = 1000L * (System.currentTimeMillis() - startTime) / duration
                normalizedTime = (0L).coerceAtLeast(normalizedTime.coerceAtMost(1000L))
                val deltaY =
                    ((fromY - toY) * scrollAnimationInterpolator.getInterpolation(normalizedTime / 1000f)).roundToInt()
                currentY = fromY - deltaY
                setBodyScroll(currentY)
            }

            if (continueRunning && toY != currentY) {
                ViewCompat.postOnAnimation(this@RefreshLayout, this)
            } else {
                refreshHeader?.onRefreshCancel()
                mState = State.NONE
                listener?.onSmoothScrollFinished()
            }
        }

        fun stop() {
            continueRunning = false
            removeCallbacks(this)
        }
    }


    enum class State {
        NONE, SCROLL_TO_BACK, AUTO_REFRESH, SCROLL_TO_REFRESH, MANUAL_SCROLLING, REFRESHING
    }

    interface OnRefreshListener {
        fun onRefresh()
    }

    /**
     * Classes that wish to override {@link RefreshLayout#canChildScrollUp()} method
     * behavior should implement this interface.
     */
    interface OnChildScrollUpCallback {
        /**
         * Callback that will be called when {@link RefreshLayout#canChildScrollUp()} method
         * is called to allow the implementer to override its behavior.
         *
         * @param parent RefreshLayout that this callback is overriding.
         * @param child  The child view of RefreshLayout.
         * @return Whether it is possible for the child view of parent layout to scroll up.
         */
        fun canChildScrollUp(parent: RefreshLayout, child: View?): Boolean
    }

    interface OnSmoothScrollFinishedListener {
        fun onSmoothScrollFinished()
    }

    companion object {
        private const val INVALID_POINTER = -1
        private const val DRAG_RATE = .6125f
        const val SMOOTH_SCROLL_DURATION_MS = 250L
        private const val TAG = "RefreshLayout"
    }
}