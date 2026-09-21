package com.rfid.rfidreader.data.api

import com.google.gson.annotations.SerializedName

data class TryOnApiResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val status: Int? = null,
    val data: List<TryOnApiItem> = emptyList(),
    val timestamp: String? = null
)

data class FittingRoomResponse(
    val status: Int,
    val message: String,
    val data: List<String>,
    val timestamp: String? = null
)

data class TryOnApiItem(
    val epc: String? = null,
    val sku: String? = null,
    val product: String? = null,
    @field:SerializedName(value = "tryOnLocation", alternate = ["tryonLocation", "location"])
    val tryOnLocation: String? = null,
    val brand: String? = null,
    val category: String? = null,
    val department: String? = null,
    val gender: String? = null,
    val color: String? = null,
    val itemSize: String? = null,
    val duration: String? = null,
    val status: String? = null,
    @field:SerializedName(value = "tryOnsStarttime", alternate = ["tryOnStarttime"])
    val tryOnsStarttime: String? = null,
    @field:SerializedName(value = "tryOnsEndtime", alternate = ["tryOnEndtime"])
    val tryOnsEndtime: String? = null,
    @field:SerializedName(value = "imageUrl", alternate = ["image", "image_url", "productImage", "mediaUrl"])
    val imageUrl: String? = null
)

data class ColorVariantsResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val status: Int? = null,
    val data: List<ColorVariantItem> = emptyList()
)

data class ColorVariantItem(
    val sku: String? = null,
    @field:SerializedName("item_size")
    val itemSize: String? = null,
    val color: String? = null,
    @field:SerializedName("image")
    val imageUrl: String? = null
)

// ─────────────────────────────────────────────────────────────────────────────
// Similar products
// ─────────────────────────────────────────────────────────────────────────────

data class SimilarProductsResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val status: Int? = null,
    val data: List<SimilarProductItem> = emptyList()
)

data class SimilarProductItem(
    val epc: String? = null,
    val sku: String? = null,
    val product: String? = null,
    val brand: String? = null,
    val category: String? = null,
    val color: String? = null,
    val itemSize: String? = null,
    val department: String? = null,
    val gender: String? = null,
    @field:SerializedName(value = "image", alternate = ["imageUrl", "image_url"])
    val imageUrl: String? = null
)

// ─────────────────────────────────────────────────────────────────────────────
// Checkout
// ─────────────────────────────────────────────────────────────────────────────

data class CheckoutRequest(
    val epc: String?,
    val sku: String?,
    @SerializedName("try_ons_location")
    val tryOnsLocation: String?,
    @SerializedName("store_id")
    val storeId: String?,
    @SerializedName("created_at")
    val createdAt: String? = null
)

data class CheckoutResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val status: Int? = null,
    val data: List<CheckoutDataItem>? = null
)

data class CheckoutDataItem(
    val createdAt: String? = null,
    val epc: String? = null,
    val sku: String? = null,
    val storeId: String? = null,
    val tryOnsLocation: String? = null
)

// ─────────────────────────────────────────────────────────────────────────────
// Item rating
// ─────────────────────────────────────────────────────────────────────────────

data class ItemRatingRequest(
    val epc: String?,
    val sku: String?,
    val rating: Float,
    val feedback: String? = null
)

data class ItemRatingResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val status: Int? = null
)

// ─────────────────────────────────────────────────────────────────────────────
// Auth
// ─────────────────────────────────────────────────────────────────────────────

data class LoginRequest(
    val usernameOrEmail: String,
    val password: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class LogoutRequest(
    val accessToken: String
)

data class LoginResponse(
    val status: Int,
    val message: String,
    val data: LoginData?,
    val timestamp: String?
)

data class LogoutResponse(
    val status: Int,
    val message: String,
    val success: Boolean? = null
)

data class LoginData(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresInSeconds: Long,
    val user: UserData?
)

data class UserData(
    val userId: String,
    val storeId: String,
    val tenantId: String?,
    val fullname: String,
    val username: String,
    val email: String,
    val phoneNo: String,
    val roleId: String,
    val roleName: String,
    val storeTenantId: String?,
    val tenantName: String,
    val trialRoomId: Int?,
    val trialRoomName: String?,
    val isActive: Boolean,
    val createdAt: String
)
