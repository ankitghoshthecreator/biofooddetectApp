package com.ankitghoshthecreator.biol

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.nio.FloatBuffer

class YoloClassifier(context: Context) {
    private val ortEnv: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val ortSession: OrtSession

    init {
        val modelBytes = context.assets.open("my_model.onnx").readBytes()
        ortSession = ortEnv.createSession(modelBytes)
    }

    fun detectObjects(bitmap: Bitmap): List<String> {
        val inputTensor = preprocessImage(bitmap)
        val results = ortSession.run(mapOf("images" to inputTensor))
        val output = results[0].value as Array<FloatArray>

        // Process YOLO output (convert predictions to labels)
        val detectedObjects = processYoloOutput(output)
        return detectedObjects
    }

    private fun preprocessImage(bitmap: Bitmap): OnnxTensor {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 640, 640, true)
        val floatBuffer = FloatBuffer.allocate(1 * 3 * 640 * 640)
        val mean = floatArrayOf(0.485f, 0.456f, 0.406f)
        val std = floatArrayOf(0.229f, 0.224f, 0.225f)

        for (y in 0 until 640) {
            for (x in 0 until 640) {
                val pixel = resizedBitmap.getPixel(x, y)
                val r = ((pixel shr 16) and 0xFF) / 255.0f
                val g = ((pixel shr 8) and 0xFF) / 255.0f
                val b = (pixel and 0xFF) / 255.0f
                floatBuffer.put((r - mean[0]) / std[0])
                floatBuffer.put((g - mean[1]) / std[1])
                floatBuffer.put((b - mean[2]) / std[2])
            }
        }
        floatBuffer.rewind()
        return OnnxTensor.createTensor(ortEnv, floatBuffer, longArrayOf(1, 3, 640, 640))
    }

    private fun processYoloOutput(output: Array<FloatArray>): List<String> {
        // Convert YOLO output to readable labels (dummy implementation)
        return listOf("Food Item 1", "Food Item 2")
    }
}
