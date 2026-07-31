package com.rfid.rfidreader.ui.recyclerview

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.FixedPreloadSizeProvider

/**
 * Provides basic image preloading for RecyclerView
 */
class TryOnPreloadModelProvider(
    private val adapter: TryOnRecyclerAdapter,
    private val requestManager: RequestManager
) : ListPreloader.PreloadModelProvider<RvTryOnItem> {

    override fun getPreloadItems(position: Int): List<RvTryOnItem> {
        val item = adapter.currentList.getOrNull(position) ?: return emptyList()
        return if (item.imageUrl.isNullOrBlank()) emptyList() else listOf(item)
    }

    override fun getPreloadRequestBuilder(item: RvTryOnItem): RequestBuilder<*>? {
        return requestManager.load(item.imageUrl)
    }
}

fun attachTryOnPreloader(
    recyclerView: RecyclerView,
    adapter: TryOnRecyclerAdapter,
    requestManager: RequestManager
) {
    val preloadSizeProvider = FixedPreloadSizeProvider<RvTryOnItem>(160, 240)
    val modelProvider = TryOnPreloadModelProvider(adapter, requestManager)
    val preloader = RecyclerViewPreloader(requestManager, modelProvider, preloadSizeProvider, 5)
    recyclerView.addOnScrollListener(preloader)
}

