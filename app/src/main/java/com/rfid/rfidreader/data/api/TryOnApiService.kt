package com.rfid.rfidreader.data.api

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Query
import okhttp3.ResponseBody
import retrofit2.Response

interface TryOnApiService {
    @GET("api/tryons/todays-try-on-items")
    suspend fun getTodaysTryOnItems(): TryOnApiResponse

    // @GET("api/tryons/fitting-room-locations")
    // suspend fun getFittingRoomLocations(): FittingRoomResponse

    // @GET("api/trial-rooms")
    // suspend fun getTrialRooms(
    //     @Query("storeId") storeId: String
    // ): FittingRoomResponse

    // @GET("api/tryons/recent-fitting-room-entries-in-5min")
    // suspend fun getRecentFittingRoomEntries(): TryOnApiResponse

    @GET("api/try-ons/recent-trial-room-items")
    suspend fun getRecentTrialRoomItems(
        @Query("storeId") storeId: String
    ): TryOnApiResponse

    @GET("api/tryons/color-variants")
    suspend fun getColorVariants(@Query("sku") sku: String): Response<ResponseBody>

    @GET("api/tryons/staff-assistance-call")
    suspend fun callStaffAssistance(
        @Query("tryOnLocation") tryOnLocation: String
    ): Response<ResponseBody>

    @GET("api/products/similar-products")
    suspend fun getSimilarProducts(
        @Query("brand") brand: String,
        @Query("gender") gender: String
    ): SimilarProductsResponse

    @POST("api/checkout-info/entries")
    suspend fun createCheckoutEntry(
        @Body request: CheckoutRequest
    ): CheckoutResponse

    @POST("api/tryons/item-rating")
    suspend fun submitItemRating(
        @Body request: ItemRatingRequest
    ): ItemRatingResponse

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>
}
