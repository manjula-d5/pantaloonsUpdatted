package com.rfid.rfidreader.data.api

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Query
import retrofit2.http.Header
import okhttp3.ResponseBody
import retrofit2.Response

interface TryOnApiService {


    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("api/try-ons/recent-trial-room-items")
    suspend fun getRecentTrialRoomItems(
        @Header("Authorization") authHeader: String,
        @Query("storeId") storeId: String
    ): TryOnApiResponse


    @POST("api/auth/logout")
    suspend fun logout(
        @Body request: LogoutRequest
    ): Response<LogoutResponse>

    // @GET("api/tryons/color-variants")
    // suspend fun getColorVariants(@Query("sku") sku: String): Response<ResponseBody>

    @GET("api/products/similar-products")
    suspend fun getSimilarProducts(
        @Query("brand") brand: String,
        @Query("gender") gender: String
    ): SimilarProductsResponse

    // @GET("api/tryons/todays-try-on-items")
    // suspend fun getTodaysTryOnItems(): TryOnApiResponse

    // @GET("api/tryons/fitting-room-locations")
    // suspend fun getFittingRoomLocations(): FittingRoomResponse

    // @GET("api/trial-rooms")
    // suspend fun getTrialRooms(
    //     @Query("storeId") storeId: String
    // ): FittingRoomResponse

    // @GET("api/tryons/recent-fitting-room-entries-in-5min")
    // suspend fun getRecentFittingRoomEntries(): TryOnApiResponse




    // @GET("api/tryons/staff-assistance-call")
    // suspend fun callStaffAssistance(
    //     @Query("tryOnLocation") tryOnLocation: String
    // ): Response<ResponseBody>



    // @POST("api/checkout-info/entries")
    // suspend fun createCheckoutEntry(
    //     @Body request: CheckoutRequest
    // ): CheckoutResponse

    // @POST("api/tryons/item-rating")
    // suspend fun submitItemRating(
    //     @Body request: ItemRatingRequest
    // ): ItemRatingResponse


}
