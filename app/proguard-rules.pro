# ======= KEEP CORE CLASSES =======

# Keep your app's entry point
-keep class com.example.steganography** { *; }

# Keep MainActivity (or whatever you named it)
-keep class **.MainActivity { *; }

# ======= COMPOSE STUFF =======
# Compose is built with Kotlin and heavy on reflection, so don’t let ProGuard kill it

# Compose UI
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Needed for lambda/metamodel stuff
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Prevent Compose preview-related crashes
-keep class androidx.compose.ui.tooling.** { *; }
-dontwarn androidx.compose.ui.tooling.**

# ======= KOTLIN METADATA =======
# Kotlin-specific shit to keep from breaking
-keep class kotlin.Metadata { *; }

# Don’t strip parameter names (useful for reflection or logging)
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod, LineNumberTable, SourceFile, LocalVariableTable, LocalVariableTypeTable, RuntimeVisibleAnnotations

# ======= FILE & URI HANDLING =======
# You use content URIs, so keep all FileProvider stuff
-keep class androidx.core.content.FileProvider { *; }
-dontwarn androidx.core.content.FileProvider

# If you ever use intent filters or share URIs
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver

# ======= IMAGE ENCODING/DECODING =======
# If using BitmapFactory, Glide, Coil, etc., keep image decoders
-keep class android.graphics.** { *; }
-dontwarn android.graphics.**

# ======= MATERIAL ICONS CORE =======
# You're using Material Icons Core
-keep class androidx.compose.material.icons.** { *; }
-dontwarn androidx.compose.material.icons.**

# ======= OPTIONAL: Disable Optimization =======
# If you're debugging issues, start with this
# -dontoptimize
