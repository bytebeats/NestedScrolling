package me.bytebeats.views.nestedscrolling

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created on 2021/9/8 20:11
 * @Version 1.0
 * @Description TO-DO
 */

class FixHeightParentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var iParentView: IParentView? = null
    private var isParentViewMadeSure = false

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measureSpecArray = measureSpecArray(widthMeasureSpec, heightMeasureSpec)
        if (measureSpecArray.size == 2) {
            setMeasuredDimension(measureSpecArray[0], measureSpecArray[1])
            super.onMeasure(measureSpecArray[0], measureSpecArray[1])
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    private fun measureSpecArray(widthMeasureSpec: Int, heightMeasureSpec: Int): IntArray {
        makeSureParentView()
        val parentView = iParentView as? View ?: return intArrayOf()
        val measureHeight = parentView.measuredHeight
        return intArrayOf(widthMeasureSpec, MeasureSpec.makeMeasureSpec(measureHeight, MeasureSpec.AT_MOST))
    }

    private fun makeSureParentView() {
        if (!isParentViewMadeSure) {
            var parent = this.parent
            while (parent != null && parent !is IParentView) {
                parent = parent.parent
            }
            iParentView = parent as IParentView?
            isParentViewMadeSure = true
        }
    }
}