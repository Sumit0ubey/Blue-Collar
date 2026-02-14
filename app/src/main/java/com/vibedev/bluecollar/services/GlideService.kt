package com.vibedev.bluecollar.services

import android.widget.ImageView
import com.bumptech.glide.Glide
import android.content.Context
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.load.engine.GlideException


object GlideService {

    fun loadImage(context: Context, imageUrl: String?, placeholder: Int, error: Int, imageView: ImageView) {
        Glide.with(context)
            .load(imageUrl)
            .placeholder(placeholder)
            .error(error)
            .into(imageView)
    }

    fun loadCircularImage(context: Context, imageUrl: String?, placeholder: Int, error: Int, imageView: ImageView) {
        Glide.with(context)
            .load(imageUrl)
            .placeholder(placeholder)
            .error(error)
            .circleCrop()
            .into(imageView)
    }

    fun loadImageWithRetry(context: Context, imageUrl: String?, placeholder: Int, error: Int, imageView: ImageView) {
        Glide.with(context)
            .load(imageUrl)
            .placeholder(placeholder)
            .error(error)
            .listener(object : RequestListener<android.graphics.drawable.Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<android.graphics.drawable.Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    imageView.setOnClickListener {
                        loadImageWithRetry(context, imageUrl, placeholder, error, imageView)
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: android.graphics.drawable.Drawable,
                    model: Any,
                    target: Target<android.graphics.drawable.Drawable>,
                    dataSource: com.bumptech.glide.load.DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    imageView.setOnClickListener(null)
                    return false
                }
            })
            .centerCrop()
            .into(imageView)
    }
}
