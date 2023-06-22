package uz.gita.firstcamera

import android.Manifest
import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import uz.gita.firstcamera.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var processFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var imageCapture: ImageCapture

    private lateinit var binding: ActivityMainBinding

    private var backOrFront: Boolean = true

    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    private val myPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        camera()
        requestPermission()
        binding.apply {
            replace.setOnClickListener {
                if (backOrFront) {
                    backOrFront = false
                    cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    backOrFront = true
                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                }
                camera()
            }
            takePhoto.setOnClickListener {
                takePhoto()
            }
        }


//        Dexter.withContext(this)
//            .withPermissions(
//                android.Manifest.permission.CAMERA,
//                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
//            )
//            .withListener(object : PermissionListener {
//                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
//
//                }
//
//                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
//
//                }
//
//                override fun onPermissionRationaleShouldBeShown(
//                    p0: PermissionRequest?,
//                    p1: PermissionToken?,
//                ) {
//
//                }
//
//            })
//            .check()

    }

    private fun camera() {
        val cameraPreview = binding.cameraPreview

        processFuture = ProcessCameraProvider.getInstance(this)

        processFuture.addListener(
            {
                val provider = processFuture.get()

                val preview = Preview.Builder().build()

                preview.setSurfaceProvider(cameraPreview.surfaceProvider)

                imageCapture = ImageCapture.Builder().build()

                val selector = cameraSelector

                try {
                    provider.unbindAll()
                    provider.bindToLifecycle(this, selector, preview, imageCapture)
                } catch (_: Exception) {

                }

            },
            ContextCompat.getMainExecutor(this)
        )

    }

    private fun takePhoto() {

        val name = SimpleDateFormat("MM-dd-yyyy", Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.d("TTT", "Saved successfull")
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.d("TTT", "Error occured in capturing ${exception.message}")

                }
            })

    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            myPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                )
            )
        } else {
            myPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                )
            )
        }
    }


}