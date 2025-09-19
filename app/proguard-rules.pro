# Keep Firebase and Google Play services
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep ML Kit
-keep class com.google.mlkit.** { *; }

# Keep Room database entities and DAO interfaces
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep interface * implements androidx.room.Dao

# Keep model classes used for serialization
-keep class com.sirimocr.app.data.model.** { *; }

# Glide generated API
-keep class com.bumptech.glide.GeneratedAppGlideModule
-keep public class * extends com.bumptech.glide.AppGlideModule
-keep public @com.bumptech.glide.annotation.GlideModule class *

-dontwarn org.apache.commons.logging.**
-dontwarn org.checkerframework.**
-dontwarn javax.annotation.**
