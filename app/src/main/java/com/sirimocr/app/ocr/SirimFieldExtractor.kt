package com.sirimocr.app.ocr

import android.graphics.Rect
import com.google.mlkit.vision.text.Text
import com.sirimocr.app.data.model.SirimData

class SirimFieldExtractor {

    private val serialPattern = Regex("TA\\d{7}")

    fun extract(text: Text): SirimData {
        val blocks = text.textBlocks
        return SirimData(
            sirimSerialNo = extractSerial(blocks),
            batchNo = extractByLabel(blocks, "BATCH"),
            brandTrademark = extractByLabel(blocks, "BRAND"),
            model = extractByLabel(blocks, "MODEL"),
            type = extractByLabel(blocks, "TYPE"),
            rating = extractByLabel(blocks, "RATING"),
            packSize = extractByLabel(blocks, "PACK")
        )
    }

    private fun extractSerial(blocks: List<Text.TextBlock>): String? {
        blocks.forEach { block ->
            block.lines.forEach { line ->
                val match = serialPattern.find(line.text)
                if (match != null) return match.value
            }
        }
        return null
    }

    private fun extractByLabel(blocks: List<Text.TextBlock>, label: String): String? {
        val labelBlock = blocks.firstOrNull { it.text.contains(label, true) } ?: return null
        val labelRect = labelBlock.boundingBox ?: return null
        blocks.forEach { block ->
            if (block == labelBlock) return@forEach
            val rect = block.boundingBox ?: return@forEach
            if (isAdjacent(labelRect, rect)) {
                return block.text.trim()
            }
        }
        return null
    }

    private fun isAdjacent(labelRect: Rect, candidate: Rect): Boolean {
        val horizontal = kotlin.math.abs(labelRect.right - candidate.left)
        val vertical = kotlin.math.abs(labelRect.centerY() - candidate.centerY())
        return horizontal < 120 && vertical < labelRect.height()
    }
}
