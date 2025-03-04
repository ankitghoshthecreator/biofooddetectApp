package com.ankitghoshthecreator.biol

import ai.onnxruntime.*
import android.content.Context
import android.graphics.Bitmap
import java.nio.FloatBuffer

class YoloClassifier(context: Context, s: String) {
    private var ortEnvironment: OrtEnvironment = OrtEnvironment.getEnvironment()
    private var ortSession: OrtSession

    init {
        try {
            val assetManager = context.assets
            val modelInputStream = assetManager.open("model.onnx")
            val modelBytes = modelInputStream.readBytes()
            modelInputStream.close()

            ortSession = ortEnvironment.createSession(modelBytes)
        } catch (e: Exception) {
            throw RuntimeException("Failed to load model: ${e.message}")
        }
    }

    fun detectObjects(bitmap: Bitmap): List<DetectionResult> {
        val inputTensor = preprocessImage(bitmap)

        val inputName = ortSession.inputNames.iterator().next()
        val inputs = mapOf(inputName to OnnxTensor.createTensor(ortEnvironment, inputTensor, longArrayOf(1, 3, 640, 640)))

        ortSession.run(inputs).use { results ->
            val rawOutput = results[0].value

            if (rawOutput is Array<*>) {
                val outputArray = (rawOutput as Array<Array<FloatArray>>)[0] // Extract first batch
                return postProcess(outputArray)
            } else {
                throw IllegalStateException("Unexpected output format: ${rawOutput?.javaClass}")
            }
        }
    }

    private fun preprocessImage(bitmap: Bitmap): FloatBuffer {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 640, 640, true) // Resize to model input
        val buffer = FloatBuffer.allocate(1 * 3 * 640 * 640) // 4D shape: [1, 3, 640, 640]
        buffer.rewind()

        val pixels = IntArray(640 * 640)
        resizedBitmap.getPixels(pixels, 0, 640, 0, 0, 640, 640)

        var pixelIndex = 0
        for (y in 0 until 640) {
            for (x in 0 until 640) {
                val pixel = pixels[pixelIndex++]

                val r = ((pixel shr 16) and 0xFF) / 255.0f
                val g = ((pixel shr 8) and 0xFF) / 255.0f
                val b = (pixel and 0xFF) / 255.0f

                buffer.put(r)
                buffer.put(g)
                buffer.put(b)
            }
        }
        buffer.rewind()
        return buffer
    }

    private fun postProcess(outputArray: Array<FloatArray>): List<DetectionResult> {
        val results = mutableListOf<DetectionResult>()

        for (i in outputArray.indices) {
            val bbox = outputArray[i] // Each row represents one detection
            val x = bbox[0]
            val y = bbox[1]
            val w = bbox[2]
            val h = bbox[3]
            val confidence = bbox[4]
            val classId = bbox.sliceArray(5 until bbox.size).indexOfFirst { it == bbox.maxOrNull() }

            if (confidence > 0.5) {
                results.add(DetectionResult(x, y, w, h, confidence, classId))
            }
        }
        return results
    }
}

data class DetectionResult(val x: Float, val y: Float, val w: Float, val h: Float, val confidence: Float, val classId: Int)
