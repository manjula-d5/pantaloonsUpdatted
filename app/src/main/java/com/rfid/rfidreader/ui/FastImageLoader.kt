package com.rfid.rfidreader.ui

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

/**
 * Simple image loader utility
 */
object FastImageLoader {
    
    val requestOptions: RequestOptions = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        .skipMemoryCache(false)

    fun loadImage(imageView: ImageView, url: String?) {
        if (url.isNullOrBlank()) {
            return
        }
        Glide.with(imageView)
            .load(url)
            .apply(requestOptions)
            .into(imageView)
    }
}

