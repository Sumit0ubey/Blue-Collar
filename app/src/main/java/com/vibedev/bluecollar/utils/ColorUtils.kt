package com.vibedev.bluecollar.utils

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import com.vibedev.bluecollar.R

object ColorExtractor {

    fun applyIconColorToBackground(
        iconDrawable: Drawable,
        viewToColor: View
    ) {
        val context = viewToColor.context
        val bitmap = iconDrawable.toBitmap(width = 64, height = 64, config = Bitmap.Config.ARGB_8888)

        Palette.from(bitmap).generate { palette ->
            val defaultColor = ContextCompat.getColor(context, R.color.default_icon_background)

            val dominantRgb = palette?.dominantSwatch?.rgb
                ?: palette?.vibrantSwatch?.rgb
                ?: palette?.mutedSwatch?.rgb
                ?: defaultColor

            val lightBackgroundColor = createLighterColor(dominantRgb)

            viewToColor.background.setTint(lightBackgroundColor)
        }
    }

    private fun createLighterColor(color: Int): Int {
        val lightnessFactor = 1.4f
        val alpha = 60

        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(color, hsl)

        hsl[2] = (hsl[2] * lightnessFactor).coerceAtMost(1.0f)

        val lightRgb = ColorUtils.HSLToColor(hsl)

        return ColorUtils.setAlphaComponent(lightRgb, alpha)
    }
}
