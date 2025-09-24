package com.sirimocr.app.ocr

import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.sirimocr.app.data.model.SirimData
import com.sirimocr.app.data.model.SirimOcrResult
import java.io.Closeable
import kotlinx.coroutines.tasks.await

class MLKitOcrProcessor(
    private val validationEngine: ValidationEngine = ValidationEngine(),
    private val fieldExtractor: SirimFieldExtractor = SirimFieldExtractor()
) : Closeable {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun process(imageProxy: ImageProxy): SirimOcrResult {
        val startTime = System.currentTimeMillis()
        val mediaImage = imageProxy.image ?: return SirimOcrResult(
            success = false,
            error = "No image",
            processingTimeMs = System.currentTimeMillis() - startTime
        )
        return try {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val text = textRecognizer.process(image).await()
            val data = fieldExtractor.extract(text)
            val validation = validationEngine.validate(data)
            val confidence = calculateConfidence(text, data, validation.confidence)
            SirimOcrResult(
                success = true,
                sirimData = data,
                confidenceScore = confidence,
                validationResult = validation,
                processingTimeMs = System.currentTimeMillis() - startTime
            )
        } catch (t: Throwable) {
            Log.e("MLKitOcr", "Processing failed", t)
            SirimOcrResult(
                success = false,
                error = t.localizedMessage,
                processingTimeMs = System.currentTimeMillis() - startTime
            )
        }
    }

    private fun calculateConfidence(text: com.google.mlkit.vision.text.Text, data: SirimData, validationScore: Float): Float {
        var total = 0f
        var count = 0
        text.textBlocks.forEach { block ->
            block.lines.forEach { line ->
                line.elements.forEach { element ->
                    total += element.confidence ?: 0f
                    count++
                }
            }
        }
        val recognitionScore = if (count == 0) 0f else total / count
        return (recognitionScore * 0.7f + validationScore * 0.3f).coerceIn(0f, 1f)
    }
    override fun close() {
        textRecognizer.close()
    }
}
