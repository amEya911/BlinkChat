package eu.tutorials.blinkchat.ui.component.chatroom

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.rememberImagePainter
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executor
import androidx.compose.ui.layout.ContentScale

@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    onPhotoCaptured: (Uri) -> Unit, // Changed to pass Uri instead of Bitmap
    lastCapturedPhoto: Uri? = null, // Changed to Uri
    onRetakePhoto: () -> Unit,
    onAccessMedia: () -> Unit,
    onSendPhoto: (Uri) -> Unit, // Changed to accept Uri
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }

    val getContent = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            onPhotoCaptured(it) // Pass Uri directly
            Log.d("CameraContent", it.toString())
        }
    }

    BackHandler(onBack = {
        onBack()
    })

    if (lastCapturedPhoto != null) {
        LastPhotoPreview(
            lastCapturedPhoto = lastCapturedPhoto, // Pass Uri
            onSendPhoto = { uri ->
                onSendPhoto(uri) // Pass Uri
            },
            onRetakePhoto = {
                onRetakePhoto()
            },
            context = context
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BottomAppBar {
                    IconButton(
                        onClick = { getContent.launch("image/*") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PermMedia,
                            contentDescription = "Access Media"
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = { capturePhoto(context, cameraController, onPhotoCaptured) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = "Capture Photo"
                        )
                    }
                }
            }
        ) { paddingValues ->
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                factory = { context ->
                    PreviewView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                        setBackgroundColor(Color.Black.toArgb())
                        scaleType = PreviewView.ScaleType.FIT_START
                    }.also { previewView ->
                        previewView.controller = cameraController
                        cameraController.bindToLifecycle(lifecycleOwner)
                    }
                }
            )
        }
    }
}

fun Bitmap.rotateBitmap(rotationDegrees: Int): Bitmap {
    val matrix = Matrix().apply {
        postRotate(-rotationDegrees.toFloat())
        postScale(-1f, -1f)
    }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

private fun capturePhoto(
    context: Context,
    cameraController: LifecycleCameraController,
    onPhotoCaptured: (Uri) -> Unit
) {
    val mainExecutor: Executor = ContextCompat.getMainExecutor(context)

    cameraController.takePicture(mainExecutor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            val correctedBitmap: Bitmap = image
                .toBitmap()
                .rotateBitmap(image.imageInfo.rotationDegrees)

            val uri = bitmapToUri(context, correctedBitmap) // Convert Bitmap to Uri
            if (uri != null) {
                onPhotoCaptured(uri) // Pass the Uri to the callback
            }
            image.close()
        }

        override fun onError(exception: ImageCaptureException) {
            Log.e("CameraContent", "Error capturing image", exception)
        }
    })
}

fun bitmapToUri(context: Context, bitmap: Bitmap): Uri? {
    // Prepare a byte array output stream to write the bitmap into
    val byteArrayOutputStream = ByteArrayOutputStream()

    // Compress the bitmap to the output stream (JPEG format)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

    // Create a ContentValues object to hold metadata for the image
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "captured_image_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/YourAppName") // Store in Pictures
    }

    // Insert the image into MediaStore and get a URI
    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    // Open an OutputStream to write the data into the content provider
    uri?.let {
        context.contentResolver.openOutputStream(it)?.use { outputStream ->
            byteArrayOutputStream.writeTo(outputStream)
        }
    }

    // Return the URI of the image in MediaStore
    return uri
}

@Composable
private fun LastPhotoPreview(
    modifier: Modifier = Modifier,
    lastCapturedPhoto: Uri, // Accept Uri instead of Bitmap
    onSendPhoto: (Uri) -> Unit,
    onRetakePhoto: () -> Unit,
    context: Context
) {
    Scaffold(
        bottomBar = {
            BottomAppBar {
                IconButton(onClick = {
                    onRetakePhoto()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Camera,
                        contentDescription = "Retake photo"
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = {
                    onSendPhoto(lastCapturedPhoto) // Send Uri directly
                }) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send photo"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // Use the Uri for image display, perhaps load it with a library like Coil for URI-based images
            Image(
                painter = rememberImagePainter(lastCapturedPhoto),
                contentDescription = "Last captured photo",
                contentScale = ContentScale.Fit
            )
        }
    }
}
