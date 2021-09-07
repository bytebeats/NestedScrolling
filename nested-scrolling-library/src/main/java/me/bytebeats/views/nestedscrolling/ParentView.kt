package me.bytebeats.views.nestedscrolling

import android.content.Context
import android.util.AttributeSet
import androidx.core.widget.NestedScrollView

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
) : NestedScrollView(context, attrs, defStyleAttr), IParentView {
    override val mParentViewHelper: ParentViewHelper
        get() = ParentViewHelper(this)

    override fun addChildView(iChildView: IChildView) {
        mParentViewHelper.addChildView(iChildView)
    }

    override fun removeChildView(iChildView: IChildView) {
        mParentViewHelper.removeChildView(iChildView)
    }

    override fun fixInconsistentChildScroll() {
        TODO("Not yet implemented")
    }
}