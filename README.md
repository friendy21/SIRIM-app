# SIRIM OCR Data Capture Application - Master Control Plan (MCP)

## Executive Summary

This Master Control Plan outlines the development of a native Android application for capturing SIRIM (Standards and Industrial Research Institute of Malaysia) certification data using real-time OCR technology with Firebase as the cloud backend solution. The application is designed with an offline-first architecture and synchronizes with Firebase services when connectivity is available.

---

## 1. Project Overview & Requirements

### 1.1 Application Purpose
- **Primary Goal**: Real-time OCR capture of SIRIM certification data.
- **Target Users**: Quality inspectors, compliance officers, warehouse personnel.
- **Deployment**: Google Play Store (Android focus).
- **Devices**: Android smartphones and tablets (API 21+ / Android 5.0+).
- **Future**: iOS version consideration after Android success.

### 1.2 Core Features
- Real-time camera OCR for SIRIM forms.
- Offline data storage and processing.
- Firebase cloud synchronization when internet available.
- Data validation and error correction.
- Export capabilities (CSV, Excel, PDF).
- Firebase Authentication and user management.
- Multi-device synchronization.
- Backup and restore functionality.

### 1.3 Business Requirements
- **Cost**: $0 monthly operational cost (Firebase Free Tier).
- **Performance**: <3 seconds OCR processing time.
- **Accuracy**: >95% OCR accuracy for clear images.
- **Scalability**: Support up to 10,000 users initially.
- **Compliance**: GDPR compliant, data privacy focused.

---

## 2. Technical Architecture - Android Studio & Firebase

### 2.1 Technology Stack

#### Frontend - Native Android
- **IDE**: Android Studio (Latest Stable).
- **Language**: Kotlin (Primary) + Java (Legacy support).
- **Min SDK**: API 21 (Android 5.0) - ~96% device coverage.
- **Target SDK**: API 34 (Android 14).
- **Architecture**: MVVM with LiveData and ViewBinding.
- **Navigation**: Android Navigation Component.
- **UI**: Material Design 3 (Material You).

#### OCR Engine Stack
- **Primary**: ML Kit Text Recognition V2 (Google) - Free, offline-capable.
- **Secondary**: ML Kit Digital Ink Recognition (for handwritten text).
- **Fallback**: Tesseract 4 OCR Engine.
- **Custom**: TensorFlow Lite model for SIRIM form layout detection.

#### Local Database & Storage
- **Primary DB**: Room Database (SQLite abstraction).
- **Preferences**: EncryptedSharedPreferences.
- **Files**: Internal storage with encryption.
- **Cache**: DiskLruCache for image processing.

#### Firebase Backend Services (Free Tier)
- **Authentication**: Firebase Authentication.
- **Database**: Cloud Firestore (NoSQL, real-time).
- **File Storage**: Firebase Storage (images, exports).
- **Analytics**: Firebase Analytics.
- **Crash Reporting**: Firebase Crashlytics.
- **Messaging**: Firebase Cloud Messaging (future feature).
- **Performance**: Firebase Performance Monitoring.

### 2.2 Architecture Diagram - Android + Firebase

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          ANDROID APPLICATION                            │
├─────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │
│  │   Camera    │  │   OCR ML    │  │   Room DB   │  │   UI/UX     │    │
│  │   Module    │  │   Kit       │  │  (SQLite)   │  │  Material3  │    │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │
│  │ Validation  │  │   Export    │  │  Encryption │  │   Offline   │    │
│  │   Engine    │  │   Manager   │  │   Manager   │  │   Manager   │    │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘    │
└─────────────────────────────────────────────────────────────────────────┘
                                    ▲ ▼ (Sync when online)
┌─────────────────────────────────────────────────────────────────────────┐
│                           FIREBASE BACKEND                              │
├─────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │
│  │  Firebase   │  │ Cloud       │  │  Firebase   │  │  Firebase   │    │
│  │    Auth     │  │ Firestore   │  │   Storage   │  │  Analytics  │    │
│  │ (FREE 50K)  │  │(FREE 20K)   │  │ (FREE 5GB)  │  │  (FREE ∞)   │    │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │
│  │ Crashlytics │  │ Performance │  │    Cloud    │  │   Future:   │    │
│  │ (FREE ∞)    │  │ (FREE ∞)    │  │ Messaging   │  │  ML Models  │    │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘    │
└─────────────────────────────────────────────────────────────────────────┘
```

### 2.3 Firebase Free Tier Limits

| Service | Free Tier Limit | Estimated Usage |
| --- | --- | --- |
| Authentication | 50,000 MAU | 1,000–5,000 users |
| Cloud Firestore | 20K writes, 50K reads, 1GB storage | 10K–15K operations/day |
| Firebase Storage | 5GB storage, 1GB/day download | 2–3GB images |
| Analytics | Unlimited events | All app usage |
| Crashlytics | Unlimited crashes | All error tracking |
| Performance | Unlimited traces | All performance data |

---

## 3. Detailed Android Architecture Implementation

### 3.1 Project Structure (Android Studio)

```
app/
├── src/main/java/com/yourcompany/sirimocr/
│   ├── ui/
│   │   ├── activities/
│   │   │   ├── MainActivity.kt
│   │   │   ├── LoginActivity.kt
│   │   │   └── SplashActivity.kt
│   │   ├── fragments/
│   │   │   ├── CameraFragment.kt
│   │   │   ├── DataEntryFragment.kt
│   │   │   ├── HistoryFragment.kt
│   │   │   └── SettingsFragment.kt
│   │   ├── adapters/
│   │   │   └── RecordsAdapter.kt
│   │   └── viewmodels/
│   │       ├── CameraViewModel.kt
│   │       ├── DataViewModel.kt
│   │       └── MainViewModel.kt
│   ├── data/
│   │   ├── database/
│   │   │   ├── entities/
│   │   │   │   ├── SirimRecord.kt
│   │   │   │   └── User.kt
│   │   │   ├── dao/
│   │   │   │   ├── SirimRecordDao.kt
│   │   │   │   └── UserDao.kt
│   │   │   └── SirimDatabase.kt
│   │   ├── repositories/
│   │   │   ├── SirimRepository.kt
│   │   │   └── AuthRepository.kt
│   │   └── models/
│   │       ├── SirimData.kt
│   │       └── ValidationResult.kt
│   ├── ocr/
│   │   ├── MLKitOCRProcessor.kt
│   │   ├── TesseractOCRProcessor.kt
│   │   ├── FieldDetector.kt
│   │   └── ValidationEngine.kt
│   ├── firebase/
│   │   ├── FirebaseManager.kt
│   │   ├── FirestoreService.kt
│   │   ├── StorageService.kt
│   │   └── AuthService.kt
│   ├── utils/
│   │   ├── NetworkUtils.kt
│   │   ├── ImageUtils.kt
│   │   ├── ExportUtils.kt
│   │   ├── EncryptionUtils.kt
│   │   └── Constants.kt
│   └── SirimOCRApplication.kt
├── src/main/res/
│   ├── layout/
│   ├── drawable/
│   ├── values/
│   └── xml/
└── build.gradle (Module: app)
```

### 3.2 Core Dependencies (build.gradle - Module: app)

```kotlin
dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.5")

    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    kapt("androidx.room:room-compiler:2.6.0")

    // CameraX
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")

    // ML Kit OCR
    implementation("com.google.mlkit:text-recognition:16.0.0")
    implementation("com.google.mlkit:digital-ink-recognition:18.1.0")

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:32.6.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-perf-ktx")

    // Tesseract OCR (Fallback)
    implementation("com.rmtheis:tess-two:9.1.0")

    // Image Processing
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Permissions
    implementation("pub.devrel:easypermissions:3.0.0")

    // Encryption
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Export Libraries
    implementation("org.apache.poi:poi:5.2.4")
    implementation("org.apache.poi:poi-ooxml:5.2.4")
    implementation("com.opencsv:opencsv:5.8")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
}
```

---

## 4. Data Structure & Database Implementation

### 4.1 SIRIM Form Fields

| Field | Max Characters | Validation Rules | Data Type |
| --- | --- | --- | --- |
| SIRIM Serial No. | 12 | Alphanumeric, format `TA0000001` | String |
| Batch No. | 200 | Alphanumeric with special characters | String |
| Brand/Trademark | 1024 | Text, allow special characters | String |
| Model | 1500 | Text, allow special characters | String |
| Type | 1500 | Text, allow special characters | String |
| Rating | 500 | Alphanumeric with units | String |
| Pack Size | 1500 | Text with units | String |

### 4.2 Room Database Schema

```kotlin
@Entity(tableName = "sirim_records")
data class SirimRecord(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "sirim_serial_no") val sirimSerialNo: String,
    @ColumnInfo(name = "batch_no") val batchNo: String? = null,
    @ColumnInfo(name = "brand_trademark") val brandTrademark: String? = null,
    @ColumnInfo(name = "model") val model: String? = null,
    @ColumnInfo(name = "type") val type: String? = null,
    @ColumnInfo(name = "rating") val rating: String? = null,
    @ColumnInfo(name = "pack_size") val packSize: String? = null,
    @ColumnInfo(name = "image_path") val imagePath: String? = null,
    @ColumnInfo(name = "confidence_score") val confidenceScore: Float = 0f,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "synced") val synced: Boolean = false,
    @ColumnInfo(name = "sync_timestamp") val syncTimestamp: Long? = null,
    @ColumnInfo(name = "user_id") val userId: String? = null,
    @ColumnInfo(name = "validation_status") val validationStatus: String = "pending"
)
```

```kotlin
@Entity(tableName = "users")
data class User(
    @PrimaryKey val uid: String,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "display_name") val displayName: String?,
    @ColumnInfo(name = "organization") val organization: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_login") val lastLogin: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_active") val isActive: Boolean = true
)
```

```kotlin
@Dao
interface SirimRecordDao {
    @Query("SELECT * FROM sirim_records WHERE user_id = :userId ORDER BY created_at DESC")
    fun getAllRecords(userId: String): LiveData<List<SirimRecord>>

    @Query("SELECT * FROM sirim_records WHERE synced = 0")
    suspend fun getUnsyncedRecords(): List<SirimRecord>

    @Query("SELECT * FROM sirim_records WHERE sirim_serial_no = :serialNo")
    suspend fun getRecordBySerialNo(serialNo: String): SirimRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: SirimRecord)

    @Update
    suspend fun updateRecord(record: SirimRecord)

    @Query("UPDATE sirim_records SET synced = 1, sync_timestamp = :timestamp WHERE id = :recordId")
    suspend fun markAsSynced(recordId: String, timestamp: Long)

    @Delete
    suspend fun deleteRecord(record: SirimRecord)
}
```

```kotlin
@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUser(uid: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}
```

```kotlin
@Database(entities = [SirimRecord::class, User::class], version = 1, exportSchema = false)
abstract class SirimDatabase : RoomDatabase() {
    abstract fun sirimRecordDao(): SirimRecordDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile private var INSTANCE: SirimDatabase? = null

        fun getDatabase(context: Context): SirimDatabase =
            INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SirimDatabase::class.java,
                    "sirim_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            db.execSQL("CREATE INDEX IF NOT EXISTS index_sirim_records_user_id ON sirim_records(user_id)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_sirim_records_synced ON sirim_records(synced)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_sirim_records_serial_no ON sirim_records(sirim_serial_no)")
        }
    }
}
```

### 4.3 Firestore Schema & Security

```
/users/{userId}
/   records/{recordId}
```

- Users collection stores profile data, preferences, and device tokens.
- Each user has a records subcollection containing SIRIM OCR entries, metadata, and storage URLs.

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      match /records/{recordId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }

    match /statistics/{document} {
      allow read: if request.auth != null;
    }
  }
}
```

---

## 5. OCR Implementation Strategy

### 5.1 ML Kit Text Recognition

```kotlin
class MLKitOCRProcessor(private val context: Context) {
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun processImage(imageProxy: ImageProxy): SirimOCRResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        return@withContext try {
            val inputImage = InputImage.fromMediaImage(
                imageProxy.image!!,
                imageProxy.imageInfo.rotationDegrees
            )

            val recognizedText = textRecognizer.process(inputImage).await()
            val extractedData = extractSirimFields(recognizedText)
            val validationResult = ValidationEngine().validateSirimData(extractedData)

            SirimOCRResult(
                success = true,
                sirimData = extractedData,
                confidenceScore = calculateConfidenceScore(recognizedText, extractedData),
                validationResult = validationResult,
                processingTimeMs = System.currentTimeMillis() - startTime
            )
        } catch (e: Exception) {
            SirimOCRResult(
                success = false,
                error = e.message,
                processingTimeMs = System.currentTimeMillis() - startTime
            )
        } finally {
            imageProxy.close()
        }
    }

    private fun extractSirimFields(recognizedText: Text): SirimData {
        val textBlocks = recognizedText.textBlocks
        val fieldExtractor = SirimFieldExtractor()
        return SirimData(
            sirimSerialNo = fieldExtractor.extractSerialNumber(textBlocks),
            batchNo = fieldExtractor.extractBatchNumber(textBlocks),
            brandTrademark = fieldExtractor.extractBrand(textBlocks),
            model = fieldExtractor.extractModel(textBlocks),
            type = fieldExtractor.extractType(textBlocks),
            rating = fieldExtractor.extractRating(textBlocks),
            packSize = fieldExtractor.extractPackSize(textBlocks)
        )
    }

    private fun calculateConfidenceScore(recognizedText: Text, extractedData: SirimData): Float {
        var totalConfidence = 0f
        var fieldCount = 0
        recognizedText.textBlocks.forEach { block ->
            block.lines.forEach { line ->
                line.elements.forEach { element ->
                    totalConfidence += element.confidence ?: 0f
                    fieldCount++
                }
            }
        }
        val avgTextConfidence = if (fieldCount > 0) totalConfidence / fieldCount else 0f
        val fieldExtractionScore = calculateFieldExtractionScore(extractedData)
        return (avgTextConfidence * 0.7f + fieldExtractionScore * 0.3f)
    }

    private fun calculateFieldExtractionScore(data: SirimData): Float {
        val fields = listOf(
            data.sirimSerialNo,
            data.batchNo,
            data.brandTrademark,
            data.model,
            data.type,
            data.rating,
            data.packSize
        )
        val filled = fields.count { !it.isNullOrBlank() }
        return filled / fields.size.toFloat()
    }
}
```

### 5.2 Field Extraction Helper

```kotlin
class SirimFieldExtractor {
    private val serialNumberPattern = Regex("""TA\d{7}""")

    fun extractSerialNumber(textBlocks: List<Text.TextBlock>): String? =
        textBlocks.asSequence()
            .flatMap { it.lines.asSequence() }
            .mapNotNull { serialNumberPattern.find(it.text)?.value }
            .firstOrNull()

    fun extractBatchNumber(textBlocks: List<Text.TextBlock>): String? =
        findAdjacentValue(textBlocks, listOf("BATCH"))

    fun extractBrand(textBlocks: List<Text.TextBlock>): String? =
        findAdjacentValue(textBlocks, listOf("BRAND", "TRADEMARK"))

    fun extractModel(textBlocks: List<Text.TextBlock>): String? =
        findAdjacentValue(textBlocks, listOf("MODEL"))

    fun extractType(textBlocks: List<Text.TextBlock>): String? =
        findAdjacentValue(textBlocks, listOf("TYPE"))

    fun extractRating(textBlocks: List<Text.TextBlock>): String? =
        findAdjacentValue(textBlocks, listOf("RATING"))

    fun extractPackSize(textBlocks: List<Text.TextBlock>): String? =
        findAdjacentValue(textBlocks, listOf("PACK", "SIZE"))

    private fun findAdjacentValue(textBlocks: List<Text.TextBlock>, keywords: List<String>): String? {
        val labelBlock = textBlocks.firstOrNull { block ->
            keywords.any { keyword -> block.text.contains(keyword, ignoreCase = true) }
        } ?: return null
        val labelRect = labelBlock.boundingBox ?: return null
        return textBlocks
            .filter { it != labelBlock }
            .filter { block ->
                block.boundingBox?.let { rect ->
                    val horizontalDistance = min(abs(labelRect.right - rect.left), abs(rect.right - labelRect.left))
                    val verticalDistance = min(abs(labelRect.bottom - rect.top), abs(rect.bottom - labelRect.top))
                    horizontalDistance < 100 || verticalDistance < 50
                } ?: false
            }
            .maxByOrNull { it.text.length }
            ?.text
            ?.takeIf { it.isNotBlank() }
    }
}
```

### 5.3 Validation Engine

```kotlin
class ValidationEngine {
    fun validateSirimData(data: SirimData): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        if (data.sirimSerialNo.isNullOrBlank()) {
            errors += ValidationError.MISSING_SERIAL_NUMBER
        } else if (!data.sirimSerialNo.matches(Regex("""TA\d{7}"""))) {
            errors += ValidationError.INVALID_SERIAL_FORMAT
        }
        if ((data.batchNo?.length ?: 0) > 200) errors += ValidationError.BATCH_TOO_LONG
        if ((data.brandTrademark?.length ?: 0) > 1024) errors += ValidationError.BRAND_TOO_LONG
        if ((data.model?.length ?: 0) > 1500) errors += ValidationError.MODEL_TOO_LONG
        if ((data.type?.length ?: 0) > 1500) errors += ValidationError.TYPE_TOO_LONG
        if ((data.rating?.length ?: 0) > 500) errors += ValidationError.RATING_TOO_LONG
        if ((data.packSize?.length ?: 0) > 1500) errors += ValidationError.PACK_SIZE_TOO_LONG
        val confidence = calculateConfidence(data, errors)
        return ValidationResult(errors.isEmpty(), errors, emptyList(), confidence)
    }

    private fun calculateConfidence(data: SirimData, errors: List<ValidationError>): Float {
        val totalFields = 7f
        val filledFields = listOf(
            data.sirimSerialNo,
            data.batchNo,
            data.brandTrademark,
            data.model,
            data.type,
            data.rating,
            data.packSize
        ).count { !it.isNullOrBlank() }
        val baseScore = filledFields / totalFields
        val penalty = errors.size * 0.2f
        return max(0f, min(1f, baseScore - penalty))
    }
}
```

### 5.4 Camera Integration with CameraX

```kotlin
class CameraFragment : Fragment() {
    private val ocrProcessor by lazy { MLKitOCRProcessor(requireContext()) }
    private val viewModel: CameraViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCamera()
        setupObservers()
    }

    private fun setupCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }
            val imageCapture = ImageCapture.Builder().build()
            val analyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                        lifecycleScope.launch {
                            val result = ocrProcessor.processImage(imageProxy)
                            viewModel.updateOCRResult(result)
                        }
                    }
                }
            cameraProvider.bindToLifecycle(viewLifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture, analyzer)
        }, ContextCompat.getMainExecutor(requireContext()))
    }
}
```

---

## 6. Firebase Integration

### 6.1 Authentication Service

```kotlin
class AuthService {
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    fun currentUser(): FirebaseUser? = auth.currentUser

    suspend fun signIn(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            authResult.user?.let { user ->
                updateUserLastLogin(user.uid)
                AuthResult.Success(user)
            } ?: AuthResult.Error("Authentication failed")
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Authentication failed")
        }
    }

    suspend fun register(email: String, password: String, displayName: String?): AuthResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            authResult.user?.let { user ->
                createUserProfile(user.uid, email, displayName)
                AuthResult.Success(user)
            } ?: AuthResult.Error("Registration failed")
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Registration failed")
        }
    }

    private suspend fun createUserProfile(uid: String, email: String, displayName: String?) {
        val profile = hashMapOf(
            "uid" to uid,
            "email" to email,
            "displayName" to displayName,
            "createdAt" to FieldValue.serverTimestamp(),
            "lastLogin" to FieldValue.serverTimestamp(),
            "isActive" to true,
            "preferences" to hashMapOf(
                "autoSync" to true,
                "exportFormat" to "csv",
                "ocrEngine" to "mlkit"
            )
        )
        firestore.collection("users").document(uid).set(profile).await()
    }

    private suspend fun updateUserLastLogin(uid: String) {
        firestore.collection("users").document(uid)
            .update("lastLogin", FieldValue.serverTimestamp())
            .await()
    }
}
```

### 6.2 Firestore Service

```kotlin
class FirestoreService {
    private val firestore = Firebase.firestore
    private val storage = Firebase.storage

    suspend fun saveRecord(record: SirimRecord): Boolean = withContext(Dispatchers.IO) {
        val userId = Firebase.auth.currentUser?.uid ?: return@withContext false
        val imageUrl = record.imagePath?.let { uploadImage(userId, record.id, it) }
        val recordMap = hashMapOf(
            "id" to record.id,
            "sirimSerialNo" to record.sirimSerialNo,
            "batchNo" to record.batchNo,
            "brandTrademark" to record.brandTrademark,
            "model" to record.model,
            "type" to record.type,
            "rating" to record.rating,
            "packSize" to record.packSize,
            "imageUrl" to imageUrl,
            "confidenceScore" to record.confidenceScore,
            "createdAt" to Timestamp(Date(record.createdAt)),
            "updatedAt" to FieldValue.serverTimestamp(),
            "validationStatus" to record.validationStatus,
            "deviceId" to Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        )
        firestore.collection("users").document(userId)
            .collection("records").document(record.id)
            .set(recordMap).await()
        true
    }

    private suspend fun uploadImage(userId: String, recordId: String, imagePath: String): String? {
        val storageRef = storage.reference.child("users/$userId/images/$recordId.jpg")
        storageRef.putFile(Uri.fromFile(File(imagePath))).await()
        return storageRef.downloadUrl.await().toString()
    }
}
```

### 6.3 Offline-First Repository

```kotlin
class SirimRepository(
    private val localDao: SirimRecordDao,
    private val firestoreService: FirestoreService,
    private val networkUtils: NetworkUtils
) {
    fun getAllRecords(userId: String): LiveData<List<SirimRecord>> = localDao.getAllRecords(userId)

    suspend fun saveRecord(record: SirimRecord): Boolean {
        localDao.insertRecord(record)
        return if (networkUtils.isConnected()) {
            val success = firestoreService.saveRecord(record)
            if (success) {
                localDao.markAsSynced(record.id, System.currentTimeMillis())
            }
            success
        } else true
    }

    suspend fun syncPendingRecords(): SyncResult {
        if (!networkUtils.isConnected()) return SyncResult.NoConnection
        val pending = localDao.getUnsyncedRecords()
        var successCount = 0
        var failureCount = 0
        pending.forEach { record ->
            val success = firestoreService.saveRecord(record)
            if (success) {
                localDao.markAsSynced(record.id, System.currentTimeMillis())
                successCount++
            } else {
                failureCount++
            }
        }
        return SyncResult.Complete(successCount, failureCount)
    }
}
```

### 6.4 Background Sync with WorkManager

```kotlin
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val database = SirimDatabase.getDatabase(applicationContext)
        val repository = SirimRepository(
            database.sirimRecordDao(),
            FirestoreService(),
            NetworkUtils(applicationContext)
        )
        return when (val result = repository.syncPendingRecords()) {
            SyncResult.NoConnection -> Result.retry()
            is SyncResult.Complete -> Result.success()
            is SyncResult.Error -> Result.failure()
        }
    }
}
```

---

## 7. User Interface & Experience

### 7.1 Navigation Flow

```
Splash → Login/Register → Dashboard → Camera Capture → OCR Processing → Data Review/Edit → Save → History → Export
```

### 7.2 Material Design 3 Components

- **MainActivity** hosts Navigation Component and BottomNavigationView.
- **CameraFragment** provides live preview with overlay guides and capture controls.
- **DataEntryFragment** allows manual adjustments with validation hints.
- **HistoryFragment** lists records with sync status indicators.
- **SettingsFragment** manages sync preferences, export options, and account actions.

### 7.3 Custom Form Overlay

```kotlin
class SirimFormOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val overlayPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.overlay_guide)
        style = Paint.Style.STROKE
        strokeWidth = 4f
        pathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
    }

    override fun onDraw(canvas: Canvas) {
        val guideWidth = width * 0.8f
        val guideHeight = height * 0.6f
        val left = (width - guideWidth) / 2
        val top = (height - guideHeight) / 2
        val right = left + guideWidth
        val bottom = top + guideHeight
        canvas.drawRect(left, top, right, bottom, overlayPaint)
    }
}
```

---

## 8. Security & Data Protection

### 8.1 Android Security Measures
- EncryptedSharedPreferences for user settings and tokens.
- AES/GCM encryption for sensitive local files using Android Keystore.
- Scoped storage compliance and secure internal directories.
- Strict network calls through HTTPS and Firebase security rules.

```kotlin
class SecureSharedPreferences(context: Context) {
    private val prefs = EncryptedSharedPreferences.create(
        "secure_prefs",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}
```

### 8.2 Firebase Security Rules

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /users/{userId}/images/{imageId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

---

## 9. Export & Data Management

### 9.1 Export Utilities

```kotlin
class ExportUtils(private val context: Context) {
    suspend fun exportToCSV(records: List<SirimRecord>, fileName: String): Uri? =
        withContext(Dispatchers.IO) {
            val csvFile = File(context.getExternalFilesDir(null), "$fileName.csv")
            CSVWriter(FileWriter(csvFile)).use { writer ->
                writer.writeNext(arrayOf("Serial", "Batch", "Brand", "Model", "Type", "Rating", "Pack Size", "Confidence", "Created", "Status"))
                records.forEach { record ->
                    writer.writeNext(arrayOf(
                        record.sirimSerialNo,
                        record.batchNo ?: "",
                        record.brandTrademark ?: "",
                        record.model ?: "",
                        record.type ?: "",
                        record.rating ?: "",
                        record.packSize ?: "",
                        record.confidenceScore.toString(),
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(record.createdAt)),
                        record.validationStatus
                    ))
                }
            }
            Uri.fromFile(csvFile)
        }
}
```

### 9.2 Backup & Restore
- Local database backup to encrypted file.
- Upload backups to Firebase Storage under user namespace.
- Restore flow downloads and decrypts backup, merging with local Room database.

---

## 10. Development Timeline & Implementation Plan

| Phase | Duration | Key Deliverables |
| --- | --- | --- |
| Phase 1: Foundation & Setup | Weeks 1–3 | Project setup, Firebase config, Room database, Auth UI |
| Phase 2: OCR & Camera | Weeks 4–7 | CameraX integration, ML Kit OCR, validation engine, OCR UI |
| Phase 3: Data Management | Weeks 8–10 | Local CRUD, Firebase sync, exports |
| Phase 4: Polish & Testing | Weeks 11–13 | UI polish, testing, performance optimization, deployment prep |

### Cost Analysis
- **Development Tools**: Android Studio (free), Firebase (free tier), GitHub (free).
- **Operational Cost**: $0/month within Firebase free limits.
- **One-time Cost**: $25 Google Play Console registration.

---

## 11. Testing & Quality Assurance

### 11.1 Testing Strategy
- **Unit Tests**: Validation engine, field extraction, repository logic.
- **Integration Tests**: Room database operations, sync flows, export utilities.
- **Instrumentation Tests**: UI navigation, Camera fragment interactions.
- **Performance Tests**: OCR timing, sync throughput, memory usage.

```kotlin
@RunWith(MockitoJUnitRunner::class)
class ValidationEngineTest {
    private val engine = ValidationEngine()

    @Test
    fun `valid serial passes`() {
        val result = engine.validateSirimData(SirimData(sirimSerialNo = "TA0000001"))
        assertTrue(result.isValid)
    }

    @Test
    fun `invalid serial fails`() {
        val result = engine.validateSirimData(SirimData(sirimSerialNo = "INVALID"))
        assertFalse(result.isValid)
    }
}
```

### 11.2 Performance Benchmarks
- App cold start < 2 seconds.
- OCR processing < 3 seconds per image.
- Sync 100 records < 30 seconds on stable connection.
- Crash-free sessions > 99.9%.

---

## 12. Deployment & Distribution

### 12.1 Release Configuration
- Enable R8 and resource shrinking for release builds.
- Configure signing configs via Gradle properties.
- ProGuard rules to keep Firebase, ML Kit, Room, and model classes.

### 12.2 Play Store Listing
- **Title**: SIRIM OCR Scanner - Data Capture Tool.
- **Short Description**: "Efficient SIRIM certificate data capture using advanced OCR technology. Works offline with cloud sync."
- **Full Description**: Highlight key features, supported fields, security, and offline capability.
- **Assets**: Screenshots for camera capture, OCR results, data review, history, export options; feature graphic 1024×500.

---

## 13. Maintenance & Future Enhancements

### 13.1 Maintenance Plan
- Monthly monitoring of Firebase usage, Crashlytics reports, dependency updates.
- Quarterly OCR accuracy tuning, UI/UX improvements, performance optimization.
- Annual Android API target updates, security audits, documentation refresh.

### 13.2 Roadmap
- **v2.0 (6 months)**: Batch processing, analytics dashboard, team collaboration, barcode support.
- **v3.0 (12 months)**: AI insights, multi-language support, voice input, AR guidance, enterprise controls.
- **Long Term**: iOS port, web dashboard, custom ML model training, integration marketplace, white-label offerings.

---

## 14. Risk Management & Mitigation

| Risk | Impact | Probability | Mitigation |
| --- | --- | --- | --- |
| OCR accuracy issues | High | Medium | Multiple OCR engines, manual review workflow, ongoing model tuning |
| Firebase quota limits | Medium | Low | Monitor usage, optimize batching, implement caching |
| Device compatibility | Medium | Low | Extensive QA across API 21–34, adaptive UI |
| Performance on low-end devices | Medium | Medium | Adjustable quality settings, background processing optimization |
| Data sync conflicts | High | Very Low | Offline-first design, conflict resolution rules, incremental sync |

---

## 15. Success Metrics & KPIs

### Technical KPIs
- OCR accuracy > 95% for clear images.
- Crash rate < 0.1%.
- Average OCR processing time < 3 seconds.
- Sync success rate > 99%.

### Business KPIs
- Daily active users, retention (Day 1/7/30).
- Play Store rating ≥ 4.0.
- Support ticket resolution < 24 hours.
- Records processed per user per day.

---

## Conclusion

This Master Control Plan provides a comprehensive blueprint for building the SIRIM OCR Data Capture Application. By combining native Android development, ML Kit OCR capabilities, and Firebase's free-tier services, the project delivers an offline-first, secure, and scalable solution tailored for compliance and quality assurance workflows. The outlined timeline, testing strategy, and maintenance roadmap ensure the application can launch successfully on the Google Play Store and evolve with future enhancements.

