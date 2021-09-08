package me.bytebeats.views.nestedscrolling

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import me.bytebeats.views.nestedscrolling.redirect.IScrollRedirect
import me.bytebeats.views.nestedscrolling.redirect.ScrollRedirect

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created on 2021/9/8 20:27
 * @Version 1.0
 * @Description TO-DO
 */

class StickyChildView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), IChildView, IScrollRedirect by ScrollRedirect() {
    private var parentScrollY = 0
    private lateinit var stickyView: View
    private var oldHeight = 0

    init {
        orientation = LinearLayout.VERTICAL
        init(this)
    }


    override val mChildViewHelper: ChildViewHelper
        get() = ChildViewHelper(this)

    override fun iParentView(): IParentView? = mChildViewHelper.iParentView()

    override fun onParentViewScrolling(scrollY: Int) {
        parentScrollY = scrollY
        updateStickyHeader()
    }

    override fun childView(): View = getChildAt(1)

    private fun updateStickyHeader() {
        val layoutParams = stickyView.layoutParams as LinearLayout.LayoutParams
        val stickyViewHeight = stickyView.height + layoutParams.bottomMargin + layoutParams.topMargin
        val stickyTop = parentScrollY.coerceAtMost(height - stickyViewHeight)
        stickyView.offsetTopAndBottom(stickyTop - stickyView.top)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        isChildrenDrawingOrderEnabled = true
        stickyView = getChildAt(0)
    }

    override fun getChildDrawingOrder(childCount: Int, drawingPosition: Int): Int {
        return if (drawingPosition == 0) childCount - 1 else drawingPosition - 1
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measureSpecArray = IChildView.measureSpecArray(widthMeasureSpec, layoutParams.height, mChildViewHelper)
        if (measureSpecArray.size == 2) {
            setMeasuredDimension(measureSpecArray[0], measureSpecArray[1])
            super.onMeasure(measureSpecArray[0], measureSpecArray[1])
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        val height = measuredHeight
        if (height < oldHeight && parentScrollY > 0) {
            val diff = parentScrollY.coerceAtMost(oldHeight - height)
            (iParentView() as View).scrollBy(0, -diff)
        }
        oldHeight = height
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (parentScrollY != 0) {
            updateStickyHeader()
        }
    }
}