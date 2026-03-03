package com.example.armeasure

import com.google.ar.sceneform.math.Vector3
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

class AIPointSmoother(private val windowSize: Int = 6) {

    private val buffer = ArrayDeque<Vector3>(windowSize)
    private val executor = Executors.newSingleThreadExecutor()
    private val latest = AtomicReference<Vector3?>(null)

    fun addSample(v: Vector3) {
        synchronized(buffer) {
            if (buffer.size >= windowSize) buffer.removeFirst()
            buffer.addLast(Vector3(v.x, v.y, v.z))
        }
    }

    fun predictAsync() {
        executor.execute {
            val avg = synchronized(buffer) {
                if (buffer.isEmpty()) return@execute
                var sx = 0f
                var sy = 0f
                var sz = 0f
                buffer.forEach { p -> sx += p.x; sy += p.y; sz += p.z }
                val n = buffer.size.toFloat()
                Vector3(sx / n, sy / n, sz / n)
            }
            latest.set(avg)
        }
    }

    fun getLatestPrediction(): Vector3? = latest.get()

    fun close() {
        executor.shutdownNow()
    }
}
