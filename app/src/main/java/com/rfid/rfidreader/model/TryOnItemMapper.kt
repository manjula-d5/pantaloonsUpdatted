package com.rfid.rfidreader.model

import com.rfid.rfidreader.data.api.TryOnApiItem
import com.rfid.rfidreader.util.AppLogger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object TryOnItemMapper {
    fun map(item: TryOnApiItem): TryOnDisplayItem {
        val eventDate = item.tryOnsStarttime.toDateOrNull()
        val endDate = item.tryOnsEndtime.toDateOrNull()
        val tagName = item.epc.cleanOrFallback("Unknown tag")
        val sku = item.sku.cleanOrFallback("Unknown SKU")
        val product = item.product.cleanOrFallback("-")
        val description = if (product == "-") sku else product
        val brand = item.brand.cleanOrFallback("-")
        val category = item.category.cleanOrFallback("-")
        val department = item.department.cleanOrFallback("-")
        val gender = item.gender.cleanOrFallback("-")
        val color = item.color.cleanOrFallback("-")
        val size = item.itemSize.cleanOrFallback("-")
        val status = item.status.cleanOrFallback(item.duration.cleanOrFallback("-"))
        val duration = item.duration.cleanOrFallback("-")
        val location = item.tryOnLocation.cleanOrFallback("-")
        val info = listOf(brand, category, gender, color)
            .filterNot { it == "-" }
            .joinToString(separator = " • ")
            .ifBlank { status }
        val startTimeText = eventDate?.toDisplayTimestamp()
            ?: item.tryOnsStarttime.cleanOrFallback("Time unavailable")
        val endTimeText = endDate?.toDisplayTimestamp()
            ?: item.tryOnsEndtime.cleanOrFallback("-")
        val timestamp = startTimeText
        val imageUrl = item.imageUrl?.trim().orEmpty().ifBlank { null }

        return TryOnDisplayItem(
            id = listOf(tagName, sku, item.tryOnsStarttime.cleanOrFallback("")).joinToString(separator = "|"),
            tagName = tagName,
            sku = sku,
            product = product,
            descp = description,
            info = info,
            size = size,
            timestamp = timestamp,
            startTimeText = startTimeText,
            endTimeText = endTimeText,
            location = location,
            department = department,
            duration = duration,
            imageUrl = imageUrl,
            brand = brand,
            category = category,
            gender = gender,
            color = color,
            status = status,
            eventTimestampMillis = eventDate?.time
        )
    }

    private fun String?.cleanOrFallback(fallback: String): String =
        this?.trim()?.takeIf { it.isNotEmpty() } ?: fallback

    private fun String?.toDateOrNull(): Date? =
        try {
            this?.trim()?.takeIf { it.isNotEmpty() }?.let {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US).apply {
                    timeZone = TimeZone.getDefault()
                }.parse(it)
            }
        } catch (e: Exception) {
            AppLogger.logError("Date parsing failed for: $this", e)
            null
        }

    private fun Date.toDisplayTimestamp(): String =
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            .format(this)
            .replace(Regex("\\b(am|pm)\\b", RegexOption.IGNORE_CASE)) {
                it.value.uppercase(Locale.getDefault())
            }
}
