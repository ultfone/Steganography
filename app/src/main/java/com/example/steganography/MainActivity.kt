package com.example.steganography

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.steganography.ui.theme.SteganographyTheme
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.ByteBuffer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SteganographyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF121214)
                ) {
                    ImagePicker()
                }
            }
        }
    }
}

@Composable
fun ImagePicker() {
    val context = LocalContext.current
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri.value = uri }

    var encryptedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var decryptedMessage by remember { mutableStateOf("No decryption yet") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Steganography",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = { imagePicker.launch("image/*") },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp)
                .clip(RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD31858))
        ) {
            Text(text = "Select Image", color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        imageUri.value?.let { uri ->
            val bitmap = uriToBitmap(context, uri)
            bitmap?.let {
                Image(
                    painter = rememberAsyncImagePainter(bitmap),
                    contentDescription = "Selected Image",
                    modifier = Modifier.size(250.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = { encryptedBitmap = encryptBitmap(bitmap, "Hello") },
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                    ) {
                        Text(text = "Encrypt", color = Color.White, fontSize = 16.sp)
                    }

                    Button(
                        onClick = { decryptedMessage = decryptBitmap(encryptedBitmap) },
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDA0F0F))
                    ) {
                        Text(text = "Decrypt", color = Color.White, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                encryptedBitmap?.let {
                    Button(
                        onClick = { saveImage(context, it) },
                        modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                    ) {
                        Text(text = "Download Image", color = Color.White, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Decrypted: $decryptedMessage", color = Color.White, fontSize = 14.sp)
            }
        }
    }
}

fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    return BitmapFactory.decodeStream(inputStream)
}

fun encryptBitmap(bitmap: Bitmap, message: String): Bitmap {
    val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val pixels = IntArray(bitmap.width * bitmap.height)
    newBitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

    val bytes = message.toByteArray()
    for (i in bytes.indices) {
        pixels[i] = (pixels[i] and 0xFFFFFF00.toInt()) or (bytes[i].toInt() and 0xFF)
    }

    newBitmap.setPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
    return newBitmap
}

fun decryptBitmap(bitmap: Bitmap?): String {
    if (bitmap == null) return "No image found"
    val pixels = IntArray(bitmap.width * bitmap.height)
    bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
    val bytes = pixels.take(100).map { (it and 0xFF).toByte() }.toByteArray()
    return String(bytes)
}

fun saveImage(context: Context, bitmap: Bitmap) {
    val file = File(context.getExternalFilesDir(null), "encrypted_image.png")
    val outputStream = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    outputStream.flush()
    outputStream.close()
}


@Preview(showBackground = true)
@Composable
fun PreviewImagePicker() {
    SteganographyTheme {
        ImagePicker()
    }
}
