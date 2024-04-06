package com.n0te15m3.quick.utils

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.security.MessageDigest

fun String.isValidUrl() =
    this.matches("^(http(s)://.)[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&/=]*)\$".toRegex())

fun ByteArray.toImageBitmap(): ImageBitmap {
    return BitmapFactory.decodeByteArray(
        this,
        0,
        this.size,
    ).asImageBitmap()
}

fun <T> List<T>.truncateHead(num: Int): List<T> {
    if (size > num) {
        return subList(num, size-1)
    }
    return this
}

fun sha256(input: String): String {
    val bytes = input.toByteArray()
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(bytes)

    // Convert the byte array to a hexadecimal string
    val hexString = StringBuilder()
    for (byte in hashBytes) {
        val hex = Integer.toHexString(0xff and byte.toInt())
        if (hex.length == 1) {
            hexString.append('0')
        }
        hexString.append(hex)
    }
    return hexString.toString()
}