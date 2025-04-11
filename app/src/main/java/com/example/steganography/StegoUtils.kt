package com.example.steganography

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import androidx.core.graphics.get
import androidx.core.graphics.set

object StegoUtils {
    private const val END_MARKER = "END"

    fun encode(bitmap: Bitmap, message: String): Bitmap {
        // Convert the bitmap to ARGB_8888 format to ensure consistent pixel format
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val messageBytes = message.toByteArray()
        val endMarkerBytes = END_MARKER.toByteArray()
        val totalBytes = messageBytes.size + endMarkerBytes.size

        // Check if the image can hold the message
        val maxBytes = mutableBitmap.width * mutableBitmap.height * 3 / 8
        if (totalBytes > maxBytes) {
            throw IllegalArgumentException("Message is too large for the image")
        }

        var byteIndex = 0
        var bitIndex = 0

        // Encode message length first (4 bytes)
        val lengthBytes = messageBytes.size.toBytes()
        for (i in 0..3) {
            for (j in 0..7) {
                val x = (byteIndex * 8 + bitIndex) % mutableBitmap.width
                val y = (byteIndex * 8 + bitIndex) / mutableBitmap.width
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
                val x = (byteIndex * 8 + bitIndex) % mutableBitmap.width
                val y = (byteIndex * 8 + bitIndex) / mutableBitmap.width
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
                val x = (byteIndex * 8 + bitIndex) % mutableBitmap.width
                val y = (byteIndex * 8 + bitIndex) / mutableBitmap.width
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
        try {
            // Convert the bitmap to ARGB_8888 format to ensure consistent pixel format
            val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val bytes = ByteArrayOutputStream()
            var byte = 0
            var bitIndex = 0
            var byteIndex = 0
            var messageLength = 0
            var lengthBytesRead = 0

            // First read the message length (4 bytes)
            while (lengthBytesRead < 4) {
                val x = (byteIndex * 8 + bitIndex) % mutableBitmap.width
                val y = (byteIndex * 8 + bitIndex) / mutableBitmap.width
                
                if (y >= mutableBitmap.height) {
                    throw IllegalArgumentException("Image too small to contain a message")
                }
                
                val pixel = mutableBitmap[x, y]
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

            // Validate message length
            val maxPossibleLength = mutableBitmap.width * mutableBitmap.height * 3 / 8
            if (messageLength <= 0) {
                throw IllegalArgumentException("Invalid message length: $messageLength")
            }
            if (messageLength > maxPossibleLength) {
                throw IllegalArgumentException("Message length ($messageLength) exceeds maximum possible length ($maxPossibleLength)")
            }

            // Read the message
            var messageBytesRead = 0
            while (messageBytesRead < messageLength) {
                val x = (byteIndex * 8 + bitIndex) % mutableBitmap.width
                val y = (byteIndex * 8 + bitIndex) / mutableBitmap.width
                
                if (y >= mutableBitmap.height) {
                    throw IllegalArgumentException("Message appears to be corrupted (reached end of image)")
                }
                
                val pixel = mutableBitmap[x, y]
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

            // Read and verify end marker
            val endMarkerBytes = END_MARKER.toByteArray()
            var endMarkerIndex = 0
            var endMarkerFound = true
            
            while (endMarkerIndex < endMarkerBytes.size) {
                val x = (byteIndex * 8 + bitIndex) % mutableBitmap.width
                val y = (byteIndex * 8 + bitIndex) / mutableBitmap.width
                
                if (y >= mutableBitmap.height) {
                    endMarkerFound = false
                    break
                }
                
                val pixel = mutableBitmap[x, y]
                val bit = getLSB(pixel)
                byte = byte or (bit shl bitIndex)

                bitIndex++
                if (bitIndex == 8) {
                    val expectedByte = endMarkerBytes[endMarkerIndex].toInt() and 0xFF
                    if (byte != expectedByte) {
                        endMarkerFound = false
                        break
                    }
                    byte = 0
                    bitIndex = 0
                    byteIndex++
                    endMarkerIndex++
                }
            }

            val result = bytes.toString("UTF-8")
            if (result.isEmpty()) {
                throw IllegalArgumentException("Decoded message is empty")
            }

            if (!endMarkerFound) {
                // If end marker is not found, still return the message but indicate it might be corrupted
                return "⚠️ Message might be corrupted: $result"
            }

            return result
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to decode message: ${e.message}")
        }
    }

    private fun setLSB(pixel: Int, bit: Int): Int {
        // Only modify the blue channel to be more resilient to image format changes
        val alpha = pixel and 0xFF000000.toInt()
        val red = pixel and 0x00FF0000
        val green = pixel and 0x0000FF00
        val blue = (pixel and 0x000000FF) and (0x000000FF - 1) or bit
        return alpha or red or green or blue
    }

    private fun getLSB(pixel: Int): Int {
        // Get the LSB from the blue channel (most reliable)
        return (pixel and 0x00000001)
    }

    private fun Int.toBytes(): ByteArray {
        return ByteArray(4) { i -> ((this ushr (i * 8)) and 0xFF).toByte() }
    }
}