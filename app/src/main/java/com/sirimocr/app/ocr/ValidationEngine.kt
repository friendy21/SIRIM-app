package com.sirimocr.app.ocr

import com.sirimocr.app.data.model.SirimData
import com.sirimocr.app.data.model.ValidationError
import com.sirimocr.app.data.model.ValidationResult
import com.sirimocr.app.data.model.ValidationWarning

class ValidationEngine {

    fun validate(data: SirimData): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        val warnings = mutableListOf<ValidationWarning>()

        val serial = data.sirimSerialNo
        if (serial.isNullOrBlank()) {
            errors.add(ValidationError.MISSING_SERIAL_NUMBER)
        } else {
            if (!serial.matches(Regex("TA\\d{7}"))) {
                errors.add(ValidationError.INVALID_SERIAL_FORMAT)
            }
            if (serial.length > 12) {
                errors.add(ValidationError.SERIAL_TOO_LONG)
            }
        }
        if ((data.batchNo?.length ?: 0) > 200) errors.add(ValidationError.BATCH_TOO_LONG)
        if ((data.brandTrademark?.length ?: 0) > 1024) errors.add(ValidationError.BRAND_TOO_LONG)
        if ((data.model?.length ?: 0) > 1500) errors.add(ValidationError.MODEL_TOO_LONG)
        if ((data.type?.length ?: 0) > 1500) errors.add(ValidationError.TYPE_TOO_LONG)
        if ((data.rating?.length ?: 0) > 500) errors.add(ValidationError.RATING_TOO_LONG)
        if ((data.packSize?.length ?: 0) > 1500) errors.add(ValidationError.PACK_SIZE_TOO_LONG)

        val filledFields = listOfNotNull(
            data.sirimSerialNo,
            data.batchNo,
            data.brandTrademark,
            data.model,
            data.type,
            data.rating,
            data.packSize
        ).count()
        val confidence = ((filledFields / 7f) - errors.size * 0.15f - warnings.size * 0.05f)
            .coerceIn(0f, 1f)
        return ValidationResult(errors.isEmpty(), errors, warnings, confidence)
    }
}
