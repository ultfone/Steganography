package com.example.steganography

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.core.content.FileProvider
import com.example.steganography.ui.theme.SteganographyTheme
import java.io.*
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SteganographyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainStegoUI()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MainStegoUI() {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var inputMessage by remember { mutableStateOf("") }
    var extractedMessage by remember { mutableStateOf<String?>(null) }
    var showDownloadButton by remember { mutableStateOf(false) }
    var resultBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
        errorMessage = null
        extractedMessage = null
        showDownloadButton = false
        resultBitmap = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "CyberStego",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .graphicsLayer {
                    shadowElevation = 12f
                }
                .drawBehind {
                    drawRect(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                }
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = inputMessage,
            onValueChange = { inputMessage = it },
            label = { Text("Enter secret message") },
            modifier = Modifier
                .fillMaxWidth()
                .blur(1.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.primary,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(15.dp))

        Button(
            onClick = { imagePicker.launch("image/*") },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("Pick Image")
        }

        Spacer(modifier = Modifier.height(15.dp))

        selectedImageUri?.let { uri ->
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Selected image",
                modifier = Modifier
                    .size(250.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, MaterialTheme.colorScheme.primary)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row {
            Button(
                onClick = {
                    encodeImage(selectedImageUri, inputMessage, context) { encodedBitmap, error ->
                        resultBitmap = encodedBitmap
                        errorMessage = error
                        showDownloadButton = encodedBitmap != null
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Encoding..." else "Encrypt")
            }

            Spacer(modifier = Modifier.width(20.dp))

            Button(
                onClick = {
                    decodeImage(selectedImageUri, context) { decodedMessage, error ->
                        extractedMessage = decodedMessage
                        errorMessage = error
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Decoding..." else "Decrypt")
            }
        }

        AnimatedVisibility(visible = errorMessage != null) {
            Column {
                Spacer(modifier = Modifier.height(15.dp))
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        AnimatedVisibility(visible = extractedMessage != null) {
            Column {
                Spacer(modifier = Modifier.height(15.dp))
                Text(
                    text = "Extracted: $extractedMessage",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        AnimatedVisibility(visible = showDownloadButton && resultBitmap != null) {
            Column {
                Spacer(modifier = Modifier.height(15.dp))
                Button(
                    onClick = {
                        resultBitmap?.let {
                            val filename = "stego_${UUID.randomUUID()}.png"
                            val file = File(context.cacheDir, filename)
                            FileOutputStream(file).use { out ->
                                it.compress(Bitmap.CompressFormat.PNG, 100, out)
                            }
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                file
                            )
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/png"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Encoded Image"))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("Share Encoded Image")
                }
            }
        }
    }
}

private fun encodeImage(
    uri: Uri?,
    message: String,
    context: android.content.Context,
    callback: (Bitmap?, String?) -> Unit
) {
    if (uri == null) {
        callback(null, "Please select an image first")
        return
    }

    try {
        val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
        val encoded = StegoUtils.encode(bitmap, message)
        callback(encoded, null)
    } catch (e: IllegalArgumentException) {
        callback(null, e.message)
    } catch (e: Exception) {
        callback(null, "An error occurred while encoding the message")
    }
}

private fun decodeImage(
    uri: Uri?,
    context: android.content.Context,
    callback: (String?, String?) -> Unit
) {
    if (uri == null) {
        callback(null, "Please select an image first")
        return
    }

    try {
        val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
        val decoded = StegoUtils.decode(bitmap)
        callback(decoded, null)
    } catch (e: Exception) {
        callback(null, "An error occurred while decoding the message")
    }
}