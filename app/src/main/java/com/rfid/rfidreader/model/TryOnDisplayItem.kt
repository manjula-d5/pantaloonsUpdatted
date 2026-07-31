package com.rfid.rfidreader.model

data class TryOnDisplayItem(
    val id: String,
    val tagName: String,
    val sku: String,
    val product: String,
    val descp: String,
    val info: String,
    val size: String,
    val timestamp: String,
    val startTimeText: String,
    val endTimeText: String,
    val location: String,
    val department: String,
    val duration: String,
    val imageUrl: String?,
    val brand: String,
    val category: String,
    val gender: String,
    val color: String,
    val status: String,
    val eventTimestampMillis: Long?
)
