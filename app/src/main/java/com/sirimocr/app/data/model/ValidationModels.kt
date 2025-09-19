package com.sirimocr.app.data.model

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<ValidationError> = emptyList(),
    val warnings: List<ValidationWarning> = emptyList(),
    val confidence: Float = 0f
)

enum class ValidationError {
    MISSING_SERIAL_NUMBER,
    INVALID_SERIAL_FORMAT,
    SERIAL_TOO_LONG,
    BATCH_TOO_LONG,
    BRAND_TOO_LONG,
    MODEL_TOO_LONG,
    TYPE_TOO_LONG,
    RATING_TOO_LONG,
    PACK_SIZE_TOO_LONG
}

enum class ValidationWarning {
    LOW_CONFIDENCE_SERIAL,
    LOW_CONFIDENCE_BATCH,
    PARTIAL_FIELD_EXTRACTION,
    IMAGE_QUALITY_LOW
}
