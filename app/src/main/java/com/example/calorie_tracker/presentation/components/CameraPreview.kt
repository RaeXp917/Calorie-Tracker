package com.example.calorie_tracker.presentation.components

import android.content.Context
import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    imageCapture: ImageCapture,
    analyzer: ImageAnalysis.Analyzer? = null,
    zoomRatio: Float = 1f
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // We hold a reference to the camera to control zoom dynamically
    var camera by remember { mutableStateOf<Camera?>(null) }

    // 1. Handle Zoom changes separately (Efficient)
    LaunchedEffect(zoomRatio, camera) {
        camera?.cameraControl?.setZoomRatio(zoomRatio)
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        update = { previewView ->
            // We only bind the camera ONCE, not every time the UI updates
            if (camera == null) {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()

                        val useCases = mutableListOf(preview, imageCapture)
                        
                        if (analyzer != null) {
                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                            imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), analyzer)
                            useCases.add(imageAnalysis)
                        }

                        // Bind and SAVE the camera instance
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            *useCases.toTypedArray()
                        )

                    } catch (exc: Exception) {
                        exc.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        }
    )
}

fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onImageCaptured: (File) -> Unit,
    onError: ((String) -> Unit)? = null
) {
    // Create unique filename with timestamp
    val timestamp = System.currentTimeMillis()
    val file = File(context.cacheDir, "scanned_food_$timestamp.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onImageCaptured(file)
            }

            override fun onError(exception: ImageCaptureException) {
                val errorMsg = when (exception.imageCaptureError) {
                    ImageCapture.ERROR_CAMERA_CLOSED -> "Camera was closed"
                    ImageCapture.ERROR_CAPTURE_FAILED -> "Capture failed"
                    ImageCapture.ERROR_FILE_IO -> "File I/O error"
                    ImageCapture.ERROR_INVALID_CAMERA -> "Invalid camera"
                    else -> "Unknown error: ${exception.message}"
                }
                exception.printStackTrace()
                onError?.invoke(errorMsg)
            }
        }
    )
}