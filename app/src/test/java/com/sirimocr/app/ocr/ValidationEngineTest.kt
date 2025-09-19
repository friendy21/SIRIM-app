package com.sirimocr.app.ocr

import com.sirimocr.app.data.model.SirimData
import com.sirimocr.app.data.model.ValidationError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationEngineTest {

    private val validationEngine = ValidationEngine()

    @Test
    fun validSerialPassesValidation() {
        val data = SirimData(sirimSerialNo = "TA1234567")
        val result = validationEngine.validate(data)
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun invalidSerialTriggersError() {
        val data = SirimData(sirimSerialNo = "INVALID")
        val result = validationEngine.validate(data)
        assertFalse(result.isValid)
        assertTrue(result.errors.contains(ValidationError.INVALID_SERIAL_FORMAT))
    }

    @Test
    fun fieldLengthConstraintsReported() {
        val data = SirimData(
            sirimSerialNo = "TA1234567",
            batchNo = "x".repeat(201),
            brandTrademark = "b".repeat(1025)
        )
        val result = validationEngine.validate(data)
        assertFalse(result.isValid)
        assertTrue(result.errors.contains(ValidationError.BATCH_TOO_LONG))
        assertTrue(result.errors.contains(ValidationError.BRAND_TOO_LONG))
        assertEquals(2, result.errors.count { it == ValidationError.BATCH_TOO_LONG || it == ValidationError.BRAND_TOO_LONG })
    }
}
