package com.rfid.rfidreader.data

import com.rfid.rfidreader.data.api.TryOnApiResponse
import com.rfid.rfidreader.data.api.TryOnApiService
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class TryOnRepositoryColorVariantsTest {

    @Test
    fun `fetchColorVariants handles wrapped data array`() = runBlocking {
        val service = FakeService(
            colorVariantPayload = """
                {
                  "success": true,
                  "data": [
                    {
                      "sku": "190000021026",
                      "item_size": "L",
                      "color": "golden yellow, black",
                      "image": "http://example.com/item.png"
                    }
                  ]
                }
            """.trimIndent()
        )
        val repository = TryOnRepository(service)

        val result = repository.fetchColorVariants("190000021026")

        assertEquals(1, result.size)
        assertEquals("190000021026", result.first().sku)
        assertEquals("L", result.first().itemSize)
    }

    @Test
    fun `fetchColorVariants handles direct object response`() = runBlocking {
        val service = FakeService(
            colorVariantPayload = """
                {
                  "sku": "190000021026",
                  "item_size": "M",
                  "color": "black",
                  "image": "http://example.com/object.png"
                }
            """.trimIndent()
        )
        val repository = TryOnRepository(service)

        val result = repository.fetchColorVariants("190000021026")

        assertEquals(1, result.size)
        assertEquals("M", result.first().itemSize)
    }

    @Test
    fun `fetchColorVariants handles direct array response`() = runBlocking {
        val service = FakeService(
            colorVariantPayload = """
                [
                  {
                    "sku": "190000021026",
                    "item_size": "S",
                    "color": "white",
                    "image": "http://example.com/arr.png"
                  }
                ]
            """.trimIndent()
        )
        val repository = TryOnRepository(service)

        val result = repository.fetchColorVariants("190000021026")

        assertTrue(result.isNotEmpty())
        assertEquals("white", result.first().color)
    }

    private class FakeService(
        private val colorVariantPayload: String,
        private val colorVariantCode: Int = 200
    ) : TryOnApiService {
        override suspend fun getTodaysTryOnItems(): TryOnApiResponse = TryOnApiResponse(data = emptyList())

        override suspend fun getRecentFittingRoomEntries(): TryOnApiResponse = TryOnApiResponse(data = emptyList())

        override suspend fun getColorVariants(sku: String): Response<okhttp3.ResponseBody> {
            val mediaType = "application/json".toMediaType()
            val body = colorVariantPayload.toResponseBody(mediaType)
            return if (colorVariantCode in 200..299) {
                Response.success(body)
            } else {
                Response.error(colorVariantCode, "error".toResponseBody(mediaType))
            }
        }

        override suspend fun callStaffAssistance(tryOnLocation: String): Response<okhttp3.ResponseBody> {
            return Response.success("{}".toResponseBody("application/json".toMediaType()))
        }

        override suspend fun getSimilarProducts(brand: String, gender: String): com.rfid.rfidreader.data.api.SimilarProductsResponse {
            return com.rfid.rfidreader.data.api.SimilarProductsResponse(success = true, data = emptyList())
        }

        override suspend fun createCheckoutEntry(request: com.rfid.rfidreader.data.api.CheckoutRequest): com.rfid.rfidreader.data.api.CheckoutResponse {
            return com.rfid.rfidreader.data.api.CheckoutResponse(success = true, message = "ok")
        }

        override suspend fun submitItemRating(request: com.rfid.rfidreader.data.api.ItemRatingRequest): com.rfid.rfidreader.data.api.ItemRatingResponse {
            return com.rfid.rfidreader.data.api.ItemRatingResponse(success = true, message = "ok", status = 200)
        }
    }
}

