package com.vibedev.bluecollar.utils

import java.util.Locale

fun String.capitalizeFirst(): String {
    if (this.isBlank()) return this
    return this.trim()
        .replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
}

fun String.capitalizeEachWord(): String {
    if (this.isBlank()) return this
    return this.trim()
        .lowercase(Locale.getDefault())
        .split(" ")
        .joinToString(" ") {
            it.replaceFirstChar { ch ->
                if (ch.isLowerCase()) ch.titlecase(Locale.getDefault()) else ch.toString()
            }
        }
}
