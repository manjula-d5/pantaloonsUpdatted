package com.rfid.rfidreader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rfid.rfidreader.BuildConfig
import com.rfid.rfidreader.data.TryOnRepository
import com.rfid.rfidreader.data.api.ColorVariantItem
import com.rfid.rfidreader.data.api.SimilarProductItem
import com.rfid.rfidreader.model.TryOnDisplayItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Context

data class TryOnUiState(
    val items: List<TryOnDisplayItem> = emptyList(),
    val selectedItemId: String? = null,
    val pendingSelectedItemId: String? = null,
    val variants: List<ColorVariantItem> = emptyList(),
    val selectedVariant: ColorVariantItem? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingVariants: Boolean = false,
    val isCallingAssistance: Boolean = false,
    val isCheckingOut: Boolean = false,
    // Item rating
    val isSubmittingRating: Boolean = false,
    val ratedSkus: Set<String> = emptySet(),
    // Similar products
    val similarProducts: List<SimilarProductItem> = emptyList(),
    val isLoadingSimilarProducts: Boolean = false,
    // When a similar product is tapped, we override what the preview shows
    val previewOverrideItem: TryOnDisplayItem? = null,
    val errorMessage: String? = null,
    val lastUpdated: String = "--",
    val availableLocations: List<String> = emptyList(),
    val selectedLocation: String = "ALL"

) {
    /** The item currently being previewed — similar-product override takes priority. */
    val featuredItem: TryOnDisplayItem?
        get() = previewOverrideItem
            ?: items.firstOrNull { it.id == selectedItemId }
            ?: if (items.size == 1) items.firstOrNull() else null

    val filteredItems: List<TryOnDisplayItem>
        get() =
            if (selectedLocation == "ALL") {
                items
            } else {
                items.filter {
                    it.location.equals(selectedLocation, ignoreCase = true)
                }
            }
}

class TryOnViewModel(
    private val repository: TryOnRepository,
    private val context: Context? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(TryOnUiState())
    val uiState: StateFlow<TryOnUiState> = _uiState.asStateFlow()

    /**
     * In-memory cache for variants to reduce API calls during single session.
     * NOTE: This cache is NOT persistent and is cleared:
     * - On manual refresh (pull-to-refresh)
     * - When API returns empty data
     * - When app is restarted
     * This ensures fresh data is always fetched from the server.
     */
    private val variantsBySkuCache = mutableMapOf<String, List<ColorVariantItem>>()

    // One-shot events for staff assistance feedback (Toast messages)
    private val _assistanceEvents = Channel<String>(Channel.BUFFERED)
    val assistanceEvents: Flow<String> = _assistanceEvents.receiveAsFlow()

    // One-shot events for checkout feedback (Toast messages)
    private val _checkoutEvents = Channel<String>(Channel.BUFFERED)
    val checkoutEvents: Flow<String> = _checkoutEvents.receiveAsFlow()

    // One-shot events for rating feedback (Toast messages)
    private val _ratingEvents = Channel<String>(Channel.BUFFERED)
    val ratingEvents: Flow<String> = _ratingEvents.receiveAsFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                refresh()
                delay(BuildConfig.TRYON_POLL_INTERVAL_MS)
            }
        }
    }

    fun selectLocation(location: String) {
        _uiState.update { state ->

            val filtered =
                if (location == "ALL")
                    state.items
                else
                    state.items.filter {
                        it.location.equals(location, ignoreCase = true)
                    }

            state.copy(
                selectedLocation = location,
                selectedItemId = filtered.firstOrNull()?.id,
                previewOverrideItem = null,
                variants = emptyList(),
                selectedVariant = null
            )
        }

        _uiState.value.selectedItemId?.let {
            selectItem(it)
        }
    }

    fun selectItem(itemId: String) {
        val currentState = _uiState.value
        if (
            (currentState.selectedItemId == itemId && currentState.variants.isNotEmpty()) ||
            (currentState.pendingSelectedItemId == itemId && currentState.isLoadingVariants)
        ) {
            return
        }

        val selectedItem = _uiState.value.items.find { it.id == itemId } ?: return
        val sku = selectedItem.sku.trim()
        if (sku.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "SKU is missing for selected item.")
            }
            return
        }

        // Fetch similar products whenever we switch to a genuinely new item
        val isNewItem = currentState.selectedItemId != itemId
        if (isNewItem || currentState.similarProducts.isEmpty()) {
            loadSimilarProductsForBrandAndGender(selectedItem.brand, selectedItem.gender)
        }

        val cachedVariants = variantsBySkuCache[sku]
        if (cachedVariants != null) {
            _uiState.update { state ->
                state.copy(
                    selectedItemId = itemId,
                    pendingSelectedItemId = null,
                    previewOverrideItem = if (isNewItem) null else state.previewOverrideItem,
                    variants = cachedVariants,
                    selectedVariant = resolveInitialVariant(
                        variants = cachedVariants,
                        selectedSku = sku,
                        preferredItem = selectedItem
                    ),
                    isLoadingVariants = false,
                    errorMessage = null
                )
            }
            return
        }

        _uiState.update { state ->
            state.copy(
                pendingSelectedItemId = itemId,
                isLoadingVariants = true,
                previewOverrideItem = if (isNewItem) null else state.previewOverrideItem,
                variants = emptyList(),
                selectedVariant = null,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            runCatching { repository.fetchColorVariants(sku) }
                .onSuccess { variants ->
                    variantsBySkuCache[sku] = variants
                    _uiState.update {
                        it.copy(
                            selectedItemId = itemId,
                            pendingSelectedItemId = null,
                            variants = variants,
                            selectedVariant = resolveInitialVariant(
                                variants = variants,
                                selectedSku = sku,
                                preferredItem = selectedItem
                            ),
                            isLoadingVariants = false,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            pendingSelectedItemId = null,
                            isLoadingVariants = false,
                            errorMessage = throwable.message ?: "Unable to load color variants."
                        )
                    }
                }
        }
    }

    fun selectVariant(variant: ColorVariantItem) {
        _uiState.update { it.copy(selectedVariant = variant) }
    }

    fun selectSize(size: String) {
        val state = _uiState.value
        val currentColor = state.selectedVariant?.color?.lowercase()?.trim()
        val variantForSize = state.variants.firstOrNull {
            it.color?.lowercase()?.trim() == currentColor &&
            it.itemSize?.trim() == size
        } ?: return
        selectVariant(variantForSize)
    }

    fun clearSelection() {
        _uiState.update { state ->
            state.copy(
                selectedItemId = null,
                pendingSelectedItemId = null,
                variants = emptyList(),
                selectedVariant = null,
                isLoadingVariants = false,
                previewOverrideItem = null,
                similarProducts = emptyList(),
                isLoadingSimilarProducts = false
            )
        }
    }

    /** Called when the user taps a card in the "More from Brand" strip. */
    fun selectSimilarProduct(product: SimilarProductItem) {
        val sku = product.sku?.trim()
        if (sku.isNullOrBlank()) return
        val displayItem = product.toDisplayItem()

        _uiState.update { state ->
            state.copy(
                previewOverrideItem = displayItem,
                variants = emptyList(),
                selectedVariant = null,
                isLoadingVariants = true,
                errorMessage = null
            )
        }

        // Fetch color variants for the clicked similar product's SKU
        viewModelScope.launch {
            runCatching { repository.fetchColorVariants(sku) }
                .onSuccess { variants ->
                    variantsBySkuCache[sku] = variants
                    _uiState.update {
                        it.copy(
                            variants = variants,
                            selectedVariant = resolveInitialVariant(
                                variants = variants,
                                selectedSku = sku,
                                preferredItem = displayItem
                            ),
                            isLoadingVariants = false,
                            errorMessage = null
                        )
                    }
                    context?.let { ctx ->
                        // Image preloading removed
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingVariants = false,
                            errorMessage = throwable.message ?: "Unable to load color variants."
                        )
                    }
                }
        }

        // Reload similar products for the new brand
        loadSimilarProductsForBrandAndGender(displayItem.brand, displayItem.gender)
    }

    fun callStaffAssistance(tryOnLocation: String = "fitting_room_1") {
        if (_uiState.value.isCallingAssistance) return
        _uiState.update { it.copy(isCallingAssistance = true) }
        viewModelScope.launch {
            _assistanceEvents.send("Thank you, Our staff will come and assist you shortly \uD83D\uDE0A")
            runCatching { repository.callStaffAssistance(tryOnLocation) }
                .onFailure {
                    _assistanceEvents.send("Could not reach staff. Please try again.")
                }
            _uiState.update { it.copy(isCallingAssistance = false) }
        }
    }

    fun checkout(tryOnLocation: String = "TRYON_1", storeId: String = "101") {
        if (_uiState.value.isCheckingOut) return

        val currentState = _uiState.value
        val featuredItem = currentState.featuredItem
        val selectedVariant = currentState.selectedVariant

        // Get EPC (stored in tagName) and SKU from featured item or selected variant
        val epc = featuredItem?.tagName
        val sku = selectedVariant?.sku ?: featuredItem?.sku
        
        if (epc.isNullOrBlank() || sku.isNullOrBlank()) {
            viewModelScope.launch {
                _checkoutEvents.send("Unable to checkout. Product information is missing.")
            }
            return
        }
        
        _uiState.update { it.copy(isCheckingOut = true) }
        viewModelScope.launch {
            runCatching {
                repository.createCheckoutEntry(
                    epc = epc,
                    sku = sku,
                    tryOnsLocation = tryOnLocation,
                    storeId = storeId
                )
            }
                .onSuccess { response ->
                    val message = response.message ?: "Checkout successful! ✓"
                    _checkoutEvents.send(message)
                }
                .onFailure { throwable ->
                    _checkoutEvents.send("Checkout failed. Please try again.")
                }
            _uiState.update { it.copy(isCheckingOut = false) }
        }
    }

    fun submitRating(epc: String?, sku: String?, rating: Float, feedback: String?) {
        val state = _uiState.value
        if (state.isSubmittingRating) return
        val skuKey = sku?.trim().orEmpty()
        if (skuKey.isNotEmpty() && state.ratedSkus.contains(skuKey)) {
            viewModelScope.launch {
                _ratingEvents.send("You already rated this product")
            }
            return
        }
        if (rating <= 0f) {
            viewModelScope.launch {
                _ratingEvents.send("Please select a rating before submitting")
            }
            return
        }
        _uiState.update { it.copy(isSubmittingRating = true) }
        viewModelScope.launch {
            runCatching {
                repository.submitItemRating(
                    epc = epc,
                    sku = sku,
                    rating = rating,
                    feedback = feedback
                )
            }
                .onSuccess { response ->
                    val ok = response.success == true || response.status == 200
                    if (ok) {
                        _uiState.update {
                            it.copy(
                                isSubmittingRating = false,
                                ratedSkus = if (skuKey.isNotEmpty()) it.ratedSkus + skuKey else it.ratedSkus
                            )
                        }
                        _ratingEvents.send("Thank you for rating this product")
                    } else {
                        _uiState.update { it.copy(isSubmittingRating = false) }
                        _ratingEvents.send(response.message ?: "Could not submit rating. Please try again.")
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isSubmittingRating = false) }
                    _ratingEvents.send("Could not submit rating. Please try again.")
                }
        }
    }

    private fun loadSimilarProductsForBrandAndGender(brand: String, gender: String) {
        if (brand.isBlank() || brand == "-" || gender.isBlank() || gender == "-") return
        _uiState.update { it.copy(isLoadingSimilarProducts = true, similarProducts = emptyList()) }
        viewModelScope.launch {
            val products: List<SimilarProductItem> = try {
                repository.fetchSimilarProducts(brand = brand, gender = gender)
            } catch (_: Exception) {
                emptyList()
            }
            _uiState.update { it.copy(similarProducts = products, isLoadingSimilarProducts = false) }
        }
    }

    fun retryNow() {
        viewModelScope.launch {
            // Clear all cached variant data to fetch fresh
            variantsBySkuCache.clear()
            refresh(forceSpinner = true)
        }
    }

    private suspend fun refresh(forceSpinner: Boolean = false) {
        val hasItems = _uiState.value.items.isNotEmpty()

        _uiState.update { state ->
            state.copy(
                isLoading = state.isLoading && !hasItems,
                isRefreshing = hasItems || forceSpinner,
                errorMessage = null
            )
        }

        runCatching {

            val items = repository.fetchRecentTryOns()

            val locations = repository.getFittingRoomLocations()

            Pair(items, locations)
        }
            .onSuccess { (items, locations) ->


                // CRITICAL: Always clear variant cache when API returns empty
                // This prevents showing old cached data when no new data exists
                if (items.isEmpty()) {
                    variantsBySkuCache.clear()
                }

                _uiState.update { state ->
                    val currentSelectedId = state.selectedItemId
                    val newSelectedItemId = when {
                        items.isEmpty() -> null
                        items.size == 1 -> items.first().id
                        currentSelectedId != null && items.any { it.id == currentSelectedId } -> currentSelectedId
                        else -> null
                    }
                    val selectionChanged = newSelectedItemId != currentSelectedId
                    
                    // If selection changed to a valid one and we don't have variants, load them
                    val fetchVariants = newSelectedItemId != null &&
                                       (newSelectedItemId != currentSelectedId || state.variants.isEmpty()) &&
                                       !state.isLoadingVariants

                    state.copy(
                        items = items,
                        availableLocations = locations,
                        selectedLocation =
                            if (state.selectedLocation == "ALL" ||
                                locations.contains(state.selectedLocation))
                                state.selectedLocation
                            else
                                "ALL",
                        selectedItemId = newSelectedItemId,
                        pendingSelectedItemId = state.pendingSelectedItemId?.takeIf { pendingId ->
                            items.any { it.id == pendingId }
                        },
                        // CRITICAL FIX: Always clear these when items is empty
                        variants = if (items.isEmpty()) emptyList() else (if (selectionChanged) emptyList() else state.variants),
                        selectedVariant = if (items.isEmpty()) null else (if (selectionChanged) null else state.selectedVariant),
                        isLoadingVariants = if (items.isEmpty()) false else (if (selectionChanged) false else state.isLoadingVariants),
                        previewOverrideItem = if (items.isEmpty()) null else (if (selectionChanged) null else state.previewOverrideItem),
                        similarProducts = if (items.isEmpty()) emptyList() else (if (selectionChanged) emptyList() else state.similarProducts),
                        isLoadingSimilarProducts = if (items.isEmpty()) false else (if (selectionChanged) false else state.isLoadingSimilarProducts),
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = null,
                        lastUpdated = formatLastUpdated(Date())
                    )
                }
                
                // Trigger variant fetch if needed
                val finalState = _uiState.value
                if (
                    finalState.selectedItemId != null &&
                    finalState.variants.isEmpty() &&
                    !finalState.isLoadingVariants
                ) {
                    selectItem(finalState.selectedItemId)
                }
            }
            .onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = throwable.message ?: "Unable to load try-on items."
                    )
                }
            }
    }

    companion object {
        private fun resolveInitialVariant(
            variants: List<ColorVariantItem>,
            selectedSku: String,
            preferredItem: TryOnDisplayItem? = null
        ): ColorVariantItem? {
            if (variants.isEmpty()) return null

            val preferredColor = preferredItem?.color?.normalize()
            val preferredSize = preferredItem?.size?.normalize()
            val preferredImage = preferredItem?.imageUrl?.normalize()

            // Priority 1: exact color + size match from the clicked item.
            variants.firstOrNull { variant ->
                variant.color.normalize() == preferredColor &&
                    variant.itemSize.normalize() == preferredSize
            }?.let { return it }

            // Priority 2: exact color + image match from the clicked item.
            variants.firstOrNull { variant ->
                variant.color.normalize() == preferredColor &&
                    variant.imageUrl.normalize() == preferredImage
            }?.let { return it }

            // Priority 3: color match keeps the same selected thumbnail/color.
            variants.firstOrNull { it.color.normalize() == preferredColor }?.let { return it }

            // Priority 4: size match if color is unavailable in variants payload.
            variants.firstOrNull { it.itemSize.normalize() == preferredSize }?.let { return it }

            // Priority 5: existing SKU-based fallback.
            return variants.firstOrNull { it.sku.equals(selectedSku, ignoreCase = true) } ?: variants.first()
        }

        private fun String?.normalize(): String? =
            this?.trim()?.lowercase(Locale.getDefault())?.ifBlank { null }

        private fun formatLastUpdated(date: Date): String =
            SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault()).format(date)

        /** Convert a SimilarProductItem into a TryOnDisplayItem for the preview. */
        private fun SimilarProductItem.toDisplayItem(): TryOnDisplayItem {
            val safeSku      = sku?.trim()?.ifBlank { "Unknown SKU" } ?: "Unknown SKU"
            val safeBrand    = brand?.trim()?.ifBlank { "-" } ?: "-"
            val safeCategory = category?.trim()?.ifBlank { "-" } ?: "-"
            val safeGender   = gender?.trim()?.ifBlank { "-" } ?: "-"
            val safeColor    = color?.trim()?.ifBlank { "-" } ?: "-"
            val safeSize     = itemSize?.trim()?.ifBlank { "-" } ?: "-"
            val safeProduct  = product?.trim()?.ifBlank { safeSku } ?: safeSku
            val safeDept     = department?.trim()?.ifBlank { "-" } ?: "-"
            val info         = listOf(safeBrand, safeCategory, safeGender, safeColor)
                .filter { it != "-" }.joinToString(" • ")
            return TryOnDisplayItem(
                id           = "similar_${safeSku}_${epc ?: System.currentTimeMillis()}",
                tagName      = epc ?: "similar",
                sku          = safeSku,
                product      = safeProduct,
                descp        = safeProduct,
                info         = info,
                size         = safeSize,
                timestamp    = "--",
                startTimeText = "--",
                endTimeText  = "--",
                location     = "-",
                department   = safeDept,
                duration     = "-",
                imageUrl     = imageUrl?.trim()?.ifBlank { null },
                brand        = safeBrand,
                category     = safeCategory,
                gender       = safeGender,
                color        = safeColor,
                status       = "-",
                eventTimestampMillis = null
            )
        }

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                TryOnViewModel(TryOnRepository.create(), context = null) as T
        }
    }
}
