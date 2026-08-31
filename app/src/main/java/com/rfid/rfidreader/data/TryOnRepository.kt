package com.rfid.rfidreader.data

import android.content.Context
import com.rfid.rfidreader.BuildConfig
import com.rfid.rfidreader.data.api.CheckoutRequest
import com.rfid.rfidreader.data.api.CheckoutResponse
import com.rfid.rfidreader.data.api.ColorVariantItem
import com.rfid.rfidreader.data.api.ItemRatingRequest
import com.rfid.rfidreader.data.api.ItemRatingResponse
import com.rfid.rfidreader.data.api.LoginRequest
import com.rfid.rfidreader.data.api.LoginResponse
import com.rfid.rfidreader.data.api.LogoutRequest
import com.rfid.rfidreader.data.api.LogoutResponse
import com.rfid.rfidreader.data.api.SimilarProductItem
import com.rfid.rfidreader.data.api.TryOnApiService
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.rfid.rfidreader.model.TryOnDisplayItem
import com.rfid.rfidreader.model.TryOnItemMapper
import com.rfid.rfidreader.util.AppLogger
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Repository for Try-On API operations.
 * 
 * NO CACHING POLICY:
 * - All API requests include no-cache headers (Cache-Control, Pragma, Expires)
 * - OkHttp cache is explicitly disabled (.cache(null))
 * - No SharedPreferences or local database storage
 * - Ensures real-time fresh data on every request
 */
class TryOnRepository(
    private val service: TryOnApiService,
    private val sessionManager: SessionManager? = null
) {
    suspend fun fetchRecentTryOns(): List<TryOnDisplayItem> {
        // Fetch only recent fitting room entries without fallback
        // When API returns empty, we should show empty state, NOT old data
        val storeId = sessionManager?.storeId?.takeIf { it.isNotBlank() } ?: "d2df29a0-fa40-47df-8c9d-0cdad35e040d"
        
        val token = sessionManager?.authToken ?: ""
        val authHeader = if (token.isEmpty()) "" else if (token.startsWith("Bearer ")) token else "Bearer $token"
        
        val recentItems = service.getRecentTrialRoomItems(authHeader, storeId).data
        
        // Remove fallback to getTodaysTryOnItems() to prevent showing stale data
        // OLD CODE: val source = if (recentItems.isNotEmpty()) recentItems else service.getTodaysTryOnItems().data
        
        return recentItems
            .map(TryOnItemMapper::map)
            .sortedByDescending { it.eventTimestampMillis ?: Long.MIN_VALUE }
    }

    fun getFittingRoomLocations(): List<String> {
        // API disabled by request
        // val storeId = sessionManager?.storeId?.takeIf { it.isNotBlank() } ?: "d2df29a0-fa40-47df-8c9d-0cdad35e040d"
        // return service.getTrialRooms(storeId).data
        return emptyList()
    }

    /* suspend fun fetchColorVariants(sku: String): List<ColorVariantItem> {
        val response = service.getColorVariants(sku)
        if (!response.isSuccessful) {
            throw IllegalStateException("Color variants request failed: ${response.code()}")
        }
        val payload = response.body()?.string().orEmpty()
        if (payload.isBlank()) return emptyList()

        val root = runCatching { JsonParser.parseString(payload) }.getOrNull() ?: return emptyList()
        return extractVariantElements(root)
            .mapNotNull { element -> runCatching { gson.fromJson(element, ColorVariantItem::class.java) }.getOrNull() }
            .map { it.normalize() }
            .filter { !it.sku.isNullOrBlank() || !it.imageUrl.isNullOrBlank() || !it.color.isNullOrBlank() }
    } */

    /* suspend fun callStaffAssistance(tryOnLocation: String = "fitting_room_1") {
        val response = service.callStaffAssistance(tryOnLocation)
        if (!response.isSuccessful) {
            throw IllegalStateException("Staff assistance call failed: ${response.code()}")
        }
    } */

    /** Returns similar products for the given brand+gender, or empty list on any error. */
    suspend fun fetchSimilarProducts(brand: String, gender: String): List<SimilarProductItem> {
        if (brand.isBlank() || brand == "-" || gender.isBlank() || gender == "-") return emptyList()
        return try {
            service.getSimilarProducts(brand = brand, gender = gender).data
        } catch (e: Exception) {
            emptyList()
        }
    }

    /* suspend fun createCheckoutEntry(
        epc: String?,
        sku: String?,
        tryOnsLocation: String?,
        storeId: String?
    ): CheckoutResponse {
        // Generate current timestamp in ISO 8601 format
        val currentTimestamp = java.text.SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            java.util.Locale.US
        ).apply {
            timeZone = java.util.TimeZone.getDefault()
        }.format(java.util.Date())
        
        val request = CheckoutRequest(
            epc = epc,
            sku = sku,
            tryOnsLocation = tryOnsLocation,
            storeId = storeId,
            createdAt = currentTimestamp
        )
        return service.createCheckoutEntry(request)
    } */

    /* suspend fun submitItemRating(
        epc: String?,
        sku: String?,
        rating: Float,
        feedback: String?
    ): ItemRatingResponse {
        val request = ItemRatingRequest(
            epc = epc,
            sku = sku,
            rating = rating,
            feedback = feedback?.takeIf { it.isNotBlank() }
        )
        return service.submitItemRating(request)
    } */

    suspend fun login(usernameOrEmail: String, password: String): Response<LoginResponse> {
        val request = LoginRequest(
            usernameOrEmail = usernameOrEmail,
            password = password
        )
        return service.login(request)
    }

    suspend fun logout(accessToken: String): Response<LogoutResponse> {
        val request = LogoutRequest(accessToken = accessToken)
        return service.logout(request)
    }

    fun getSessionStoreId(): String? = sessionManager?.storeId

    companion object {
        private val gson = Gson()

        fun create(context: Context? = null): TryOnRepository {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            // Custom interceptor to log all network activity to file
            val fileLoggingInterceptor = Interceptor { chain ->
                val request = chain.request()
                AppLogger.log("NETWORK REQ: ${request.method} ${request.url}")
                
                val response = try {
                    chain.proceed(request)
                } catch (e: Exception) {
                    AppLogger.logError("NETWORK FAILED: ${request.url}", e)
                    throw e
                }

                AppLogger.log("NETWORK RESP: ${response.code} ${request.url}")
                response
            }
            
            val sessionManager = context?.let { SessionManager(it) }
            
            val authInterceptor = Interceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                val token = sessionManager?.authToken ?: BuildConfig.TRYON_AUTH_TOKEN.trim()
                
                if (token.isNotEmpty()) {
                    val headerValue = if (token.contains(' ')) token else "Bearer $token"
                    requestBuilder.header("Authorization", headerValue)
                }
                chain.proceed(requestBuilder.build())
            }
            
            // No-cache interceptor to disable all caching and always fetch fresh data
            val noCacheInterceptor = Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Cache-Control", "no-cache, no-store, must-revalidate")
                    .addHeader("Pragma", "no-cache")
                    .addHeader("Expires", "0")
                    .build()
                
                val response = chain.proceed(request)
                response.newBuilder()
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("Expires", "0")
                    .build()
            }
            
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(fileLoggingInterceptor) // Add file logger
                .addInterceptor(authInterceptor)
                .addInterceptor(noCacheInterceptor)  // Add no-cache interceptor
                .addInterceptor(logging)
                .cache(null)  // Disable OkHttp cache completely
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.TRYON_BASE_URL.ensureTrailingSlash())
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return TryOnRepository(
                service = retrofit.create(TryOnApiService::class.java),
                sessionManager = sessionManager
            )
        }

        private fun String.ensureTrailingSlash(): String =
            if (endsWith('/')) this else "$this/"

        private fun extractVariantElements(root: JsonElement): List<JsonElement> {
            val candidate = if (root is JsonObject && root.has("data")) root.get("data") else root
            return when (candidate) {
                is JsonArray -> candidate.toList()
                is JsonObject -> listOf(candidate)
                else -> emptyList()
            }
        }

        private fun JsonArray.toList(): List<JsonElement> =
            buildList {
                for (index in 0 until this@toList.size()) add(this@toList.get(index))
            }

        private fun ColorVariantItem.normalize(): ColorVariantItem =
            copy(
                sku = sku?.trim()?.ifBlank { null },
                itemSize = itemSize?.trim()?.ifBlank { null },
                color = color?.trim()?.ifBlank { null },
                imageUrl = imageUrl?.trim()?.ifBlank { null }
            )
    }
}
