package com.example.steganography

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import androidx.core.graphics.get
import androidx.core.graphics.set

object StegoUtils {
    private const val END_MARKER = "END"

    fun encode(bitmap: Bitmap, message: String): Bitmap {
        val messageBytes = message.toByteArray()
        val endMarkerBytes = END_MARKER.toByteArray()
        val totalBytes = messageBytes.size + endMarkerBytes.size

        // Check if the image can hold the message
        val maxBytes = bitmap.width * bitmap.height * 3 / 8
        if (totalBytes > maxBytes) {
            throw IllegalArgumentException("Message is too large for the image")
        }

        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        var byteIndex = 0
        var bitIndex = 0

        // Encode message length first (4 bytes)
        val lengthBytes = messageBytes.size.toBytes()
        for (i in 0..3) {
            for (j in 0..7) {
                val x = (byteIndex * 8 + bitIndex) % bitmap.width
                val y = (byteIndex * 8 + bitIndex) / bitmap.width
                val pixel = mutableBitmap[x, y]

                val bit = (lengthBytes[i].toInt() ushr j) and 1
                val newPixel = setLSB(pixel, bit)
                mutableBitmap[x, y] = newPixel

                bitIndex++
                if (bitIndex == 8) {
                    bitIndex = 0
                    byteIndex++
                }
            }
        }

        // Encode message
        for (byte in messageBytes) {
            for (i in 0..7) {
                val x = (byteIndex * 8 + bitIndex) % bitmap.width
                val y = (byteIndex * 8 + bitIndex) / bitmap.width
                val pixel = mutableBitmap[x, y]

                val bit = (byte.toInt() ushr i) and 1
                val newPixel = setLSB(pixel, bit)
                mutableBitmap[x, y] = newPixel

                bitIndex++
                if (bitIndex == 8) {
                    bitIndex = 0
                    byteIndex++
                }
            }
        }

        // Encode end marker
        for (byte in endMarkerBytes) {
            for (i in 0..7) {
                val x = (byteIndex * 8 + bitIndex) % bitmap.width
                val y = (byteIndex * 8 + bitIndex) / bitmap.width
                val pixel = mutableBitmap[x, y]

                val bit = (byte.toInt() ushr i) and 1
                val newPixel = setLSB(pixel, bit)
                mutableBitmap[x, y] = newPixel

                bitIndex++
                if (bitIndex == 8) {
                    bitIndex = 0
                    byteIndex++
                }
            }
        }

        return mutableBitmap
    }

    fun decode(bitmap: Bitmap): String {
        val bytes = ByteArrayOutputStream()
        var byte = 0
        var bitIndex = 0
        var byteIndex = 0
        var messageLength = 0
        var lengthBytesRead = 0

        // First read the message length (4 bytes)
        while (lengthBytesRead < 4) {
            val x = (byteIndex * 8 + bitIndex) % bitmap.width
            val y = (byteIndex * 8 + bitIndex) / bitmap.width
            val pixel = bitmap[x, y]

            val bit = getLSB(pixel)
            byte = byte or (bit shl bitIndex)

            bitIndex++
            if (bitIndex == 8) {
                messageLength = messageLength or (byte shl (lengthBytesRead * 8))
                byte = 0
                bitIndex = 0
                byteIndex++
                lengthBytesRead++
            }
        }

        // Read the message
        var messageBytesRead = 0
        while (messageBytesRead < messageLength) {
            val x = (byteIndex * 8 + bitIndex) % bitmap.width
            val y = (byteIndex * 8 + bitIndex) / bitmap.width
            val pixel = bitmap[x, y]

            val bit = getLSB(pixel)
            byte = byte or (bit shl bitIndex)

            bitIndex++
            if (bitIndex == 8) {
                bytes.write(byte)
                byte = 0
                bitIndex = 0
                byteIndex++
                messageBytesRead++
            }
        }

        return bytes.toString("UTF-8")
    }

    private fun setLSB(pixel: Int, bit: Int): Int {
        val alpha = pixel and 0xFF000000.toInt()
        val red = (pixel and 0x00FF0000) and (0x00FF0000 - 1) or (bit shl 16)
        val green = (pixel and 0x0000FF00) and (0x0000FF00 - 1) or (bit shl 8)
        val blue = (pixel and 0x000000FF) and (0x000000FF - 1) or bit
        return alpha or red or green or blue
    }

    private fun getLSB(pixel: Int): Int {
        return (pixel and 0x00000001)
    }

    private fun Int.toBytes(): ByteArray {
        return ByteArray(4) { i -> ((this ushr (i * 8)) and 0xFF).toByte() }
    }
}