package com.ankitghoshthecreator.biol

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var btnOpenCamera: Button
    private lateinit var imageView: ImageView
    private var capturedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnOpenCamera = findViewById(R.id.btnOpenCamera)
        imageView = findViewById(R.id.imageView) // Make sure you have an ImageView in your XML

        btnOpenCamera.setOnClickListener {
            openCamera()
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val imageBitmap = result.data!!.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)

            showUploadDialog()
        } else {
            Toast.makeText(this, "Image capture failed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }

    private fun showUploadDialog() {
        AlertDialog.Builder(this)
            .setTitle("Upload Image")
            .setMessage("Do you want to upload this image?")
            .setPositiveButton("Yes") { _, _ ->
                // TODO: Implement what to do if the user selects 'Yes'
                Toast.makeText(this, "Uploading Image...", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No") { _, _ ->
                openCamera() // Reopen camera if user selects 'No'
            }
            .setCancelable(false)
            .show()
    }
}