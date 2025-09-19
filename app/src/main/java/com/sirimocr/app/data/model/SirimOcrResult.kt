package com.sirimocr.app.data.model

data class SirimOcrResult(
    val success: Boolean,
    val sirimData: SirimData? = null,
    val confidenceScore: Float = 0f,
    val validationResult: ValidationResult? = null,
    val error: String? = null,
    val processingTimeMs: Long = 0L
)
