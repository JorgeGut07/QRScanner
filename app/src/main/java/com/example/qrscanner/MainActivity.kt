package com.example.qrscanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var scanButton: Button
    private lateinit var closeCameraButton: ImageButton
    private lateinit var resultText: TextView

    private var cameraProvider: ProcessCameraProvider? = null
    private var isProcessing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        scanButton = findViewById(R.id.scanButton)
        closeCameraButton = findViewById(R.id.closeCameraButton)
        resultText = findViewById(R.id.resultText)
        val btnIrCrud = findViewById<Button>(R.id.btnIrCrud)
        btnIrCrud.setOnClickListener {
            val intent = Intent(this, Crud::class.java)
            startActivity(intent)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            setupCameraProvider()
        }

        scanButton.setOnClickListener {
            previewView.visibility = View.VISIBLE
            closeCameraButton.visibility = View.VISIBLE
            startCamera()
        }

        closeCameraButton.setOnClickListener {
            stopCamera()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) setupCameraProvider()
        else Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
    }

    private fun setupCameraProvider() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        val provider = cameraProvider ?: return

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val scanner = BarcodeScanning.getClient()

        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        analysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
            if (!isProcessing) {
                isProcessing = true
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            if (barcodes.isNotEmpty()) {
                                val value = barcodes.first().rawValue
                                resultText.text = value ?: "No se leyó texto"
                                stopCamera()
                            }
                        }
                        .addOnFailureListener {
                            Log.e("QR", "Error: ${it.message}", it)
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                            isProcessing = false
                        }
                } else {
                    imageProxy.close()
                    isProcessing = false
                }
            } else {
                imageProxy.close()
            }
        }

        provider.unbindAll()
        provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
    }

    private fun stopCamera() {
        cameraProvider?.unbindAll()
        previewView.visibility = View.GONE
        closeCameraButton.visibility = View.GONE
        isProcessing = false
    }
}
