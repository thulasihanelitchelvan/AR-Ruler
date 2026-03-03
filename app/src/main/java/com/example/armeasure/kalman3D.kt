package com.example.armeasure

import com.google.ar.sceneform.math.Vector3

class Kalman3D {

    private val kx = OneDKalman()
    private val ky = OneDKalman()
    private val kz = OneDKalman()

    fun reset() {
        kx.reset()
        ky.reset()
        kz.reset()
    }

    fun update(v: Vector3): Vector3 =
        Vector3(
            kx.update(v.x),
            ky.update(v.y),
            kz.update(v.z)
        )
}

class OneDKalman(
    private val q: Float = 0.0005f,
    private val r: Float = 0.02f
) {
    private var x = 0f
    private var p = 1f
    private var initialized = false

    fun reset() {
        initialized = false
        p = 1f
    }

    fun update(z: Float): Float {
        if (!initialized) {
            x = z
            initialized = true
        }
        p += q
        val k = p / (p + r)
        x += k * (z - x)
        p *= (1 - k)
        return x
    }
}
