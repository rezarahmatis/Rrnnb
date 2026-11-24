package com.example.rendering

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.simulation.AquaticSimulationEngine

/**
 * Custom OpenGL ES 3.0 GLSurfaceView supporting subtle multi-touch ripple creation,
 * fluid drag trails, and optional haptic feedback.
 */
@SuppressLint("ViewConstructor")
class PelagiaGLSurfaceView(
    context: Context,
    private val engine: AquaticSimulationEngine,
    private val onTapAmbientToggle: () -> Unit
) : GLSurfaceView(context) {

    private val glRenderer = AquaticGLRenderer(engine)
    private var lastRippleTimeMs = 0L
    private var downX = 0f
    private var downY = 0f
    private var downTimeMs = 0L

    init {
        setEGLContextClientVersion(3)
        setEGLConfigChooser(8, 8, 8, 0, 0, 0)
        holder.setFormat(PixelFormat.OPAQUE)
        preserveEGLContextOnPause = true
        setRenderer(glRenderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val w = width.coerceAtLeast(1).toFloat()
        val h = height.coerceAtLeast(1).toFloat()
        val normX = (event.x / w).coerceIn(0f, 1f)
        val normY = (event.y / h).coerceIn(0f, 1f)
        val now = System.currentTimeMillis()

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                downTimeMs = now
                lastRippleTimeMs = now
                engine.enqueueTouchRipple(normX, normY, 1.0f)
                if (engine.currentSettings.hapticFeedback) {
                    performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (now - lastRippleTimeMs > 95L) {
                    lastRippleTimeMs = now
                    engine.enqueueTouchRipple(normX, normY, 0.58f)
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                val dx = event.x - downX
                val dy = event.y - downY
                val isShortTap = (now - downTimeMs) < 260L && (dx * dx + dy * dy) < 625f
                if (isShortTap && engine.currentSettings.ambientMode) {
                    onTapAmbientToggle()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun releaseRenderer() {
        queueEvent { glRenderer.release() }
    }
}

@Composable
fun AquaticSurfaceView(
    engine: AquaticSimulationEngine,
    onExitAmbient: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val glView = remember(engine) {
        PelagiaGLSurfaceView(
            context = context,
            engine = engine,
            onTapAmbientToggle = onExitAmbient
        )
    }

    DisposableEffect(lifecycleOwner, glView) {
        var paused = false
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (paused) {
                        glView.onResume()
                        paused = false
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    if (!paused) {
                        glView.onPause()
                        paused = true
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (!paused) {
                glView.onPause()
            }
            glView.releaseRenderer()
        }
    }

    AndroidView(
        factory = { glView },
        modifier = modifier
    )
}
