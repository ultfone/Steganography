package com.example.steganography

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.example.steganography.ui.theme.SteganographyTheme

class MainActivity : ComponentActivity() {
    external fun encryptMessage(input: String): String
    external fun decryptMessage(input: String): String

    companion object {
        init {
            System.loadLibrary("steganography") // Load C++ library
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SteganographyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF121214)
                ) {
                    ImagePicker(this)
                }
            }
        }
    }
}

@Composable
fun ImagePicker(activity: MainActivity) {
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri.value = uri
    }

    var encryptedText by remember { mutableStateOf("") }
    var decryptedText by remember { mutableStateOf("") }

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
            Card(
                modifier = Modifier
                    .size(250.dp)
                    .clip(RoundedCornerShape(12.dp)),
                elevation = CardDefaults.elevatedCardElevation(6.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Selected Image",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = { encryptedText = activity.encryptMessage("Hello") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .height(45.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                ) {
                    Text(text = "Encrypt", color = Color.White, fontSize = 16.sp)
                }

                Button(
                    onClick = { decryptedText = activity.decryptMessage(encryptedText) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                        .height(45.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDA0F0F))
                ) {
                    Text(text = "Decrypt", color = Color.White, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Encrypted: $encryptedText",
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                text = "Decrypted: $decryptedText",
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewImagePicker() {
    SteganographyTheme {
        ImagePicker(MainActivity())
    }
}
