package com.example.armeasure

import com.google.ar.core.Anchor
import kotlin.math.sqrt

class MeasurementEngine {

    enum class State {
        EMPTY,
        ONE_POINT,
        TWO_POINTS
    }

    private var anchor1: Anchor? = null
    private var anchor2: Anchor? = null

    private var frozenDistance: Float = 0f

    var state: State = State.EMPTY
        private set

    fun addAnchor(anchor: Anchor) {
        when (state) {
            State.EMPTY -> {
                anchor1 = anchor
                state = State.ONE_POINT
            }

            State.ONE_POINT -> {
                anchor2 = anchor
                state = State.TWO_POINTS
            }

            State.TWO_POINTS -> {}
        }
    }

    fun getAnchor1(): Anchor? = anchor1
    fun getAnchor2(): Anchor? = anchor2

    fun getDistanceMeters(): Float {
        return if (state == State.TWO_POINTS) {
            calculateDistance()
        } else {
            0f
        }
    }

    private fun calculateDistance(): Float {
        val a = anchor1 ?: return 0f
        val b = anchor2 ?: return 0f

        val p1 = a.pose
        val p2 = b.pose

        val dx = p1.tx() - p2.tx()
        val dy = p1.ty() - p2.ty()
        val dz = p1.tz() - p2.tz()

        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    fun undo() {
        when (state) {
            State.TWO_POINTS -> {
                anchor2?.detach()
                anchor2 = null
                frozenDistance = 0f
                state = State.ONE_POINT
            }

            State.ONE_POINT -> {
                anchor1?.detach()
                anchor1 = null
                state = State.EMPTY
            }

            State.EMPTY -> {}
        }
    }

    fun reset() {
        anchor1?.detach()
        anchor2?.detach()

        anchor1 = null
        anchor2 = null
        frozenDistance = 0f
        state = State.EMPTY
    }
}