package com.example.armeasure

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Point
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.ar.core.*
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.math.Vector3
import kotlin.math.abs
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private lateinit var arFragment: MyArFragment
    private lateinit var overlay: MeasureOverlayView
    private lateinit var distanceText: TextView
    private lateinit var unitToggle: TextView

    private val engine = MeasurementEngine()
    private val trackingController = ARTrackingController()

    private var previewPoint: Vector3? = null
    private var useCm = true
    private var firstPlaneDetectedTime: Long = 0L

    private val anchor1Smoother = Kalman3D()
    private val anchor2Smoother = Kalman3D()

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) setupAr() else finish()
        }

    private lateinit var loadingOverlay: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        overlay = findViewById(R.id.measureOverlay)
        distanceText = findViewById(R.id.distanceText)
        unitToggle = findViewById(R.id.unitToggle)
        loadingOverlay = findViewById(R.id.loadingOverlay)

        unitToggle.setOnClickListener {
            useCm = !useCm
            unitToggle.text = if (useCm) "CM" else "IN"
            updateDistanceText()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        } else setupAr()
    }

    private fun setupAr() {
        arFragment =
            supportFragmentManager.findFragmentById(R.id.arFragment) as MyArFragment

        arFragment.planeDiscoveryController.hide()
        arFragment.planeDiscoveryController.setInstructionView(null)
        arFragment.arSceneView.planeRenderer.isVisible = false

        arFragment.arSceneView.scene.addOnUpdateListener { _: FrameTime ->
            val frame = arFragment.arSceneView.arFrame ?: return@addOnUpdateListener

            if (frame.camera.trackingState != TrackingState.TRACKING) {
                loadingOverlay.visibility = View.VISIBLE
                distanceText.text = "Initializing AR..."
                return@addOnUpdateListener
            }

            val center = getScreenCenter()

            previewPoint = trackingController.getPreview(
                frame,
                center.x.toFloat(),
                center.y.toFloat()
            )

            if (previewPoint == null) {
                loadingOverlay.visibility = View.VISIBLE
                distanceText.text = "Move phone to detect surface"
                firstPlaneDetectedTime = 0L
            } else {
                loadingOverlay.visibility = View.GONE

                if (firstPlaneDetectedTime == 0L) {
                    firstPlaneDetectedTime = System.currentTimeMillis()
                }
            }

            redraw()
        }

        arFragment.arSceneView.scene.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                handleTap()
            }
            true
        }
    }

    private fun handleTap() {
        if (engine.state == MeasurementEngine.State.EMPTY) {
            if (System.currentTimeMillis() - firstPlaneDetectedTime < 500) {
                return
            }
        }

        if (engine.state == MeasurementEngine.State.TWO_POINTS) return

        val frame = arFragment.arSceneView.arFrame ?: return
        val center = getScreenCenter()

        val hit = frame.hitTest(center.x.toFloat(), center.y.toFloat())
            .firstOrNull {
                it.trackable is Plane &&
                        it.trackable.trackingState == TrackingState.TRACKING &&
                        isAllowedPlane(it.trackable as Plane)
            } ?: return

        val anchor = hit.createAnchor()
        engine.addAnchor(anchor)

        vibrate()

        if (engine.state == MeasurementEngine.State.TWO_POINTS) {
            findViewById<View>(R.id.crosshair).visibility = View.GONE
        }

        redraw()
    }

    private fun redraw() {
        val a1 = engine.getAnchor1()
        val a2 = engine.getAnchor2()

        val pts2D = mutableListOf<PointF>()
        var label: String? = null

        if (a1 != null) {
            val raw = Vector3(
                a1.pose.tx(),
                a1.pose.ty(),
                a1.pose.tz()
            )
            val smooth = anchor1Smoother.update(raw)
            worldToScreen(smooth)?.let { pts2D.add(it) }
        }

        if (a2 != null) {
            val raw = Vector3(
                a2.pose.tx(),
                a2.pose.ty(),
                a2.pose.tz()
            )
            val smooth = anchor2Smoother.update(raw)

            worldToScreen(smooth)?.let { pts2D.add(it) }

            val meters = engine.getDistanceMeters()
            label = formatDistance(meters)
        }

        overlay.setMeasurement(pts2D, label)

        if (engine.state == MeasurementEngine.State.ONE_POINT && previewPoint != null) {
            val p1 = engine.getAnchor1()!!
            overlay.setPreview(
                worldToScreen(Vector3(
                    p1.pose.tx(),
                    p1.pose.ty(),
                    p1.pose.tz()
                )),
                worldToScreen(previewPoint)
            )
        } else {
            overlay.clearPreview()
        }

        updateDistanceText()
    }

    private fun updateDistanceText() {
        if (engine.state == MeasurementEngine.State.TWO_POINTS) {
            val meters = engine.getDistanceMeters()
            distanceText.text = formatDistance(meters)
        } else {
            distanceText.text = "0.0 ${if (useCm) "cm" else "in"}"
        }
    }

    private fun formatDistance(meters: Float): String {
        return if (useCm) {
            String.format("%.1f cm", meters * 100f)
        } else {
            String.format("%.1f in", meters * 39.3701f)
        }
    }

    private fun worldToScreen(v: Vector3?): PointF? =
        try {
            if (v == null) null
            else {
                val p = arFragment.arSceneView.scene.camera.worldToScreenPoint(v)
                PointF(p.x, p.y)
            }
        } catch (_: Exception) {
            null
        }

    private fun getScreenCenter(): Point {
        val v = findViewById<View>(android.R.id.content)
        return Point(v.width / 2, v.height / 2)
    }

    private fun isAllowedPlane(plane: Plane): Boolean {
        val n = plane.centerPose.yAxis
        val horizontal = abs(n[1]) > 0.9f
        val vertical = abs(n[1]) < 0.3f
        return horizontal || vertical
    }

    private fun vibrate() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            vibrator.vibrate(40)
        }
    }

    // -------------------------
    // Public UI Actions
    // -------------------------

    fun onUndoClicked(view: View) {
        engine.undo()
        findViewById<View>(R.id.crosshair).visibility = View.VISIBLE
        redraw()
        anchor1Smoother.reset()
        anchor2Smoother.reset()
    }

    fun onRetrackClicked(view: View) {
        engine.reset()
        trackingController.clear()
        findViewById<View>(R.id.crosshair).visibility = View.VISIBLE

        val session = arFragment.arSceneView.session ?: return
        session.pause()
        session.resume()

        redraw()
        anchor1Smoother.reset()
        anchor2Smoother.reset()
    }
}