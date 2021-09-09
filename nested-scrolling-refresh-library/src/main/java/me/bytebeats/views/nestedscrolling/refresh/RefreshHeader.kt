package me.bytebeats.views.nestedscrolling.refresh

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import com.airbnb.lottie.LottieAnimationView

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created on 2021/9/9 10:29
 * @Version 1.0
 * @Description TO-DO
 */

class RefreshHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AbstractRefreshHeader(context, attrs, defStyleAttr) {

    private val lottieAnimationView by lazy { findViewById<LottieAnimationView>(R.id.lottie_animation_view) }
    var pullingLottie: String? = null
    var refreshingLottie: String? = null
    private var mState: State = State.STILL

    init {
        inflate(context, R.layout.layout_nested_scrolling_refresh, this)
        pullingLottie = "pulling.json"
        refreshingLottie = "refreshing.json"
    }

    override fun onPull(scrollValue: Float) {
        if (scrollValue > 0 && mState == State.STILL) {
            mState = State.PREPARED
            lottieAnimationView.setAnimation(pullingLottie)
            lottieAnimationView.progress = 0F
        }
        if (mState == State.PREPARED) {
            var process = 0F
            if (scrollValue > height / 2F) {
                process = (scrollValue - height / 2F) / height
            }
            if (process < 1F) {
                lottieAnimationView.progress = process
            } else {
                onRefreshStart()
            }
        }
    }

    override fun alreadyToRefresh(alreadyToRefresh: Boolean) {
    }

    override fun onRefreshStart() {
        if (mState == State.REFRESHING) {
            return
        }
        lottieAnimationView.setAnimation(refreshingLottie)
        lottieAnimationView.repeatCount = ValueAnimator.INFINITE
        lottieAnimationView.playAnimation()
        mState = State.REFRESHING
    }

    override fun onRefreshFinish() {
        mState = State.STILL
        lottieAnimationView.clearAnimation()
    }

    override fun onRefreshCancel() {
        if (mState != State.PREPARED) {
            return
        }
        lottieAnimationView.clearAnimation()
    }

    override fun isMovable(): Boolean = true

    enum class State {
        STILL, PREPARED, REFRESHING
    }
}