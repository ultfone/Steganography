package com.example.steganography

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.size
import androidx.core.view.WindowCompat


import com.example.steganography.ui.theme.SteganographyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WindowCompat.setDecorFitsSystemWindows(window,false)
            SteganographyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ImagePicker(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}


@Composable
fun ImagePicker(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val counter = remember { mutableStateOf(0) }
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val gridTypeShi = rememberLauncherForActivityResult(
         contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri.value = uri
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { gridTypeShi.launch("image/*") }) {
                Text("Pick an Image")
            }
            Button(onClick = { counter.value++ }) {
                Text("Count: ${counter.value}")
            }


            imageUri.value?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(top = 16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SteganographyTheme {
        ImagePicker()
    }
}