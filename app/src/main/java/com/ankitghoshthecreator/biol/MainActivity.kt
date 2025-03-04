package com.ankitghoshthecreator.biol

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var btnOpenCamera: Button
    private lateinit var imageView: ImageView
    private lateinit var resultTextView: TextView
    private var yoloClassifier: YoloClassifier? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnOpenCamera = findViewById(R.id.btnOpenCamera)
        imageView = findViewById(R.id.imageView)
        resultTextView = findViewById(R.id.resultTextView)

        try {
            yoloClassifier = YoloClassifier(this, "model.onnx")
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading model: ${e.message}", Toast.LENGTH_LONG).show()
        }

        btnOpenCamera.setOnClickListener {
            openCamera()
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val imageBitmap = result.data!!.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
            showUploadDialog(imageBitmap)
        } else {
            Toast.makeText(this, "Image capture failed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }

    private fun showUploadDialog(imageBitmap: Bitmap) {
        AlertDialog.Builder(this)
            .setTitle("Upload Image")
            .setMessage("Do you want to upload this image?")
            .setPositiveButton("Yes") { _, _ ->
                classifyImage(imageBitmap)
            }
            .setNegativeButton("No") { _, _ ->
                openCamera()
            }
            .setCancelable(false)
            .show()
    }

    private fun classifyImage(imageBitmap: Bitmap) {
        val detectedObjects = yoloClassifier?.detectObjects(imageBitmap) ?: emptyList()
        if (detectedObjects.isNotEmpty()) {
            val resultText = "Detected: ${detectedObjects.joinToString(", ")}"
            resultTextView.text = resultText
        } else {
            resultTextView.text = "No objects detected!"
        }
    }
}
