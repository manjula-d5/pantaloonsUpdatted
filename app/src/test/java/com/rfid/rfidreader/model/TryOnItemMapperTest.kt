package com.rfid.rfidreader.model

import com.rfid.rfidreader.data.api.TryOnApiItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TryOnItemMapperTest {
    @Test
    fun map_populatesDisplayFieldsFromApiItem() {
        val item = TryOnApiItem(
            epc = "E2801191A50300636F706D09",
            sku = "190000021031",
            product = "Pantaloon",
            tryOnLocation = "fitting_room_2",
            brand = "MEN APPAREL",
            category = "MEN APPAREL",
            department = "Apparel",
            gender = "MEN",
            color = "Teal Blue",
            itemSize = "M",
            duration = "00h 00m",
            status = "IN",
            tryOnsStarttime = "2026-05-25T18:15:01.498",
            tryOnsEndtime = "2026-05-25T18:26:41.943",
            imageUrl = "https://content.pantaloons.com/PT2026/8909429175783_2.jpg"
        )

        val mapped = TryOnItemMapper.map(item)

        assertEquals("E2801191A50300636F706D09", mapped.tagName)
        assertEquals("190000021031", mapped.sku)
        assertEquals("Pantaloon", mapped.descp)
        assertEquals("Pantaloon", mapped.product)
        assertEquals("MEN APPAREL • MEN APPAREL • MEN • Teal Blue", mapped.info)
        assertEquals("M", mapped.size)
        assertEquals("IN", mapped.status)
        assertEquals("00h 00m", mapped.duration)
        assertEquals("fitting_room_2", mapped.location)
        assertEquals("Apparel", mapped.department)
        assertEquals("MEN APPAREL", mapped.brand)
        assertEquals("MEN APPAREL", mapped.category)
        assertEquals("MEN", mapped.gender)
        assertEquals("Teal Blue", mapped.color)
        assertEquals("25 May 2026, 06:15 PM", mapped.timestamp)
        assertEquals("25 May 2026, 06:15 PM", mapped.startTimeText)
        assertEquals("25 May 2026, 06:26 PM", mapped.endTimeText)
        assertEquals("https://content.pantaloons.com/PT2026/8909429175783_2.jpg", mapped.imageUrl)
        assertEquals("E2801191A50300636F706D09|190000021031|2026-05-25T18:15:01.498", mapped.id)
    }

    @Test
    fun map_handlesMissingTimestampGracefully() {
        val item = TryOnApiItem(
            epc = "tag-1",
            sku = "sku-1",
            product = "Product",
            status = "IN",
            tryOnsStarttime = "bad timestamp"
        )

        val mapped = TryOnItemMapper.map(item)

        assertEquals("bad timestamp", mapped.timestamp)
        assertNull(mapped.eventTimestampMillis)
    }
}
