package com.example.armeasure

import com.google.ar.core.*
import com.google.ar.sceneform.math.Vector3
import kotlin.math.sqrt

class ARTrackingController {

    private val kalman = Kalman3D()
    private var lastOutput: Vector3? = null
    private var lockedPlane: Plane? = null

    companion object {
        private const val MAX_ALLOWED_JUMP = 0.03f  // 3cm
    }

    fun clear() {
        kalman.reset()
        lastOutput = null
        lockedPlane = null
    }

    fun getPreview(
        frame: Frame,
        screenX: Float,
        screenY: Float
    ): Vector3? {

        if (frame.camera.trackingState != TrackingState.TRACKING) {
            return lastOutput
        }

        val hit = frame.hitTest(screenX, screenY)
            .firstOrNull { hit ->
                val plane = hit.trackable as? Plane ?: return@firstOrNull false

                plane.trackingState == TrackingState.TRACKING &&
                        plane.isPoseInPolygon(hit.hitPose) &&
                        (lockedPlane == null || plane == lockedPlane)
            } ?: return lastOutput

        val plane = hit.trackable as Plane

        if (lockedPlane == null) {
            lockedPlane = plane
        }

        val pose = hit.hitPose
        val raw = Vector3(pose.tx(), pose.ty(), pose.tz())

        if (lastOutput == null) {
            lastOutput = raw
            return raw
        }

        val movement = distance(raw, lastOutput!!)

        if (movement > MAX_ALLOWED_JUMP) {
            return lastOutput
        }

        val filtered = kalman.update(raw)
        lastOutput = filtered
        return filtered
    }

    private fun distance(a: Vector3, b: Vector3): Float =
        sqrt(
            (a.x - b.x) * (a.x - b.x) +
                    (a.y - b.y) * (a.y - b.y) +
                    (a.z - b.z) * (a.z - b.z)
        )
}