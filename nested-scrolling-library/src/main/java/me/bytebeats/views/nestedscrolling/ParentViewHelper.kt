package me.bytebeats.views.nestedscrolling

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created on 2021/9/7 20:46
 * @Version 1.0
 * @Description 嵌套滚动父 View 帮助类
 */

class ParentViewHelper(private val parentView: View) : View.OnLayoutChangeListener {
    private val children = mutableListOf<View>()
    private val topChildren = mutableListOf<Pair<View, Int>>()
    private val hitRectMap = mutableMapOf<View, Rect>()
    private val pendingViews = mutableListOf<View>()
    private var needUpdate = true
    private val pair = MutablePair<View?, Int>(null, 0)

    private val cmd = Runnable {
        (parentView as IParentView).fixInconsistentChildScroll()
    }

    fun addChildView(iChildView: IChildView) {
        var currentView = iChildView as View
        children.add(currentView)
        while (currentView != parentView) {
            currentView.addOnLayoutChangeListener(this)
            currentView = currentView.parent as View
        }
        needUpdate = true
    }

    fun removeChildView(iChildView: IChildView) {
        children.remove(iChildView as View)
        iChildView.removeOnLayoutChangeListener(this)
        needUpdate = true
    }

    fun closestChild(top: Int, before: Boolean): MutablePair<View?, Int> {
        updateVisibleRect()
        val idx = topChildren.binarySearch { it.second - top }
        if (idx >= 0) {
            return pair.update(topChildren[idx].first, top)
        } else {
            val insertIdx = -idx - 1
            if (before) {
                if (insertIdx > 0) {
                    return pair.update(topChildren[insertIdx - 1].first, topChildren[insertIdx - 1].second)
                }
            } else {
                if (insertIdx in topChildren.indices) {
                    return pair.update(topChildren[insertIdx].first, topChildren[insertIdx].second)
                }
            }
        }
        return pair.update(null, 0)
    }

    fun topChild(top: Int): MutablePair<View?, Int> {
        val idx = topChildren.binarySearch { it.second - top }
        if (idx >= 0) {
            return pair.update(topChildren[idx].first, top)
        } else {
            val insertIdx = -idx - 1
            if (insertIdx > 0) {
                val child = topChildren[insertIdx - 1].first
                if (insertIdx in 1..hitRectMap[child]!!.bottom) {
                    return pair.update(child, topChildren[insertIdx - 1].second)
                }
            }
        }
        return pair.update(null, 0)
    }

    private fun updateVisibleRect() {
        if (!needUpdate) return
        needUpdate = true
        hitRectMap.clear()

        for (child in children) {
            var currentView = child
            pendingViews.clear()
            while (currentView != parentView) {
                if (hitRectMap.containsKey(currentView)) {
                    for (pendingView in pendingViews) {
                        addRect(hitRectMap[pendingView]!!, hitRectMap[currentView]!!)
                    }
                    break
                }
                val rect = Rect()
                currentView.getHitRect(rect)
                hitRectMap[currentView] = rect
                for (pendingView in pendingViews) {
                    addRect(hitRectMap[pendingView]!!, rect)
                }
                pendingViews.add(currentView)
                currentView = currentView.parent as ViewGroup
            }
        }

        topChildren.clear()
        val rect = Rect()
        for (child in children) {
            parentView.getHitRect(rect)
            val currentRect = hitRectMap[child]!!
            if (isRectOverlay(rect, currentRect)) {
                topChildren.add(child to currentRect.top)
            }
        }
        topChildren.sortBy { it.second }
    }

    private fun addRect(childRect: Rect, parentRect: Rect) {
        childRect.offset(parentRect.left, parentRect.top)
    }

    private fun isRectOverlay(rect1: Rect, rect2: Rect): Boolean {
        return rect1.left < rect2.right && rect2.left < rect1.right
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
        needUpdate = true
        parentView.removeCallbacks(cmd)
        parentView.post(cmd)
    }
}