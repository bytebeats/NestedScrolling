package me.bytebeats.views.nestedscrolling.refresh

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created on 2021/9/8 21:00
 * @Version 1.0
 * @Description TO-DO
 */

interface IRefreshHeader {
    fun setHeight(height: Int)

    fun setMargins(left: Int, top: Int, right: Int, bottom: Int)

    fun setMargins(margin: Int) {
        setMargins(margin, margin, margin, margin)
    }

    fun onPull(scrollValue: Float)

    fun alreadyToRefresh(alreadyToRefresh: Boolean)

    fun onRefreshStart()

    fun onRefreshFinish()

    fun onRefreshCancel()

    fun isMovable(): Boolean
}