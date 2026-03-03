package com.example.armeasure

import android.Manifest
import com.google.ar.core.*
import com.google.ar.sceneform.ux.ArFragment

class MyArFragment : ArFragment() {

    override fun getSessionConfiguration(session: Session): Config {
        val config = Config(session)

        config.focusMode = Config.FocusMode.AUTO
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP

        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.depthMode = Config.DepthMode.AUTOMATIC
        }

        arSceneView.setupSession(session)
        return config
    }

    fun disableInstantPlacement() {
        val session = arSceneView.session ?: return
        val config = session.config
        config.instantPlacementMode = Config.InstantPlacementMode.DISABLED
        session.configure(config)
    }

    override fun getAdditionalPermissions(): Array<String> =
        arrayOf(Manifest.permission.CAMERA)
}
