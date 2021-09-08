package me.bytebeats.views.nestedscrolling

data class MutablePair<F, S>(var first: F, var second: S)

fun <F, S> MutablePair<F, S>.update(first: F, second: S): MutablePair<F, S> {
    this.first = first
    this.second = second
    return this
}
