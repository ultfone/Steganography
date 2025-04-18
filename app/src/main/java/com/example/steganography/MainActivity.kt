package com.example.steganography

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.asImageBitmap
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
                    MainUI()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainUI() {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var inputMessage by remember { mutableStateOf("") }
    var extractedMessage by remember { mutableStateOf<String?>(null) }
    var showDownloadButton by remember { mutableStateOf(false) }
    var resultBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedImageFormat by remember { mutableStateOf<Bitmap.CompressFormat?>(null) }

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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = "Steganography",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

        OutlinedTextField(
            value = inputMessage,
            onValueChange = { inputMessage = it },
            label = { Text("Enter secret message") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        )

        ElevatedButton(
            onClick = { imagePicker.launch("image/*") },
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Select Image (JPG/JPEG/PNG/WEBP)", style = MaterialTheme.typography.titleMedium)
        }

        selectedImageUri?.let { uri ->
            val bitmap = remember(uri) {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val mimeType = context.contentResolver.getType(uri)
                    selectedImageFormat = when (mimeType) {
                        "image/jpeg" -> Bitmap.CompressFormat.JPEG
                        "image/jpg" -> Bitmap.CompressFormat.JPEG
                        "image/webp" -> Bitmap.CompressFormat.WEBP
                        else -> Bitmap.CompressFormat.PNG
                    }
                    BitmapFactory.decodeStream(inputStream)
                }
            }
            bitmap?.let {
                Card(
                    modifier = Modifier
                        .size(250.dp)
                        .padding(8.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Selected image",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    isLoading = true
                    encodeImage(selectedImageUri, inputMessage, context) { encodedBitmap, error ->
                        resultBitmap = encodedBitmap
                        errorMessage = error
                        showDownloadButton = encodedBitmap != null
                        isLoading = false
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading && selectedImageUri != null && inputMessage.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (isLoading) "Encoding..." else "Encrypt")
            }

            Button(
                onClick = {
                    isLoading = true
                    decodeImage(selectedImageUri, context) { decodedMessage, error ->
                        extractedMessage = decodedMessage
                        errorMessage = error
                        isLoading = false
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading && selectedImageUri != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(if (isLoading) "Decoding..." else "Decrypt")
            }
        }

        AnimatedVisibility(
            visible = errorMessage != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = extractedMessage != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Text(
                    text = "Decoded Message: $extractedMessage",
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = showDownloadButton && resultBitmap != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        resultBitmap?.let {
                            val format = Bitmap.CompressFormat.PNG
                            val extension = "png"
                            val filename = "stego_${UUID.randomUUID()}.$extension"
                            val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            val file = File(downloads, filename)
                            FileOutputStream(file).use { out ->
                                it.compress(format, 100, out)
                            }
                            errorMessage = "Image saved to Downloads folder"
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Spacer(Modifier.width(8.dp))
                    Text("Save")
                }

                FilledTonalButton(
                    onClick = {
                        resultBitmap?.let {
                            val format = Bitmap.CompressFormat.PNG
                            val extension = "png"
                            val filename = "stego_${UUID.randomUUID()}.$extension"
                            val file = File(context.cacheDir, filename)
                            FileOutputStream(file).use { out ->
                                it.compress(format, 100, out)
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
                    //insert
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                    Spacer(Modifier.width(8.dp))
                    Text("Share")
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
    if (message.isBlank()) {
        callback(null, "Please enter a message to encode")
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
        if (decoded.isBlank()) {
            callback(null, "No hidden message found in this image")
        } else {
            callback(decoded, null)
        }
    } catch (e: IllegalArgumentException) {
        callback(null, e.message ?: "Invalid message format")
    } catch (e: Exception) {
        callback(null, "Error: ${e.message}")
    }
}