# Troubleshooting Guide

Common issues and fixes when running the SIRIM OCR Android app.

## Build Failures

- **`google-services.json is missing`** – Copy your Firebase configuration file into `app/google-services.json` and resync Gradle.
- **`Gradle wrapper properties found but gradle-wrapper.jar missing`** – Run `gradle wrapper --gradle-version 8.2` from the project root to regenerate the wrapper JAR.
- **Dependency resolution errors** – Make sure you are using Android Studio Giraffe or newer and have Google Maven Repository enabled in SDK Manager.

## Runtime Issues

- **Camera preview is blank** – Confirm the app has camera permission in Settings. If running on an emulator, ensure it supports camera hardware.
- **Capture button disabled** – The button is disabled while the app is saving a record. Wait a moment or check the toast message for errors.
- **Images rotated incorrectly** – The capture pipeline now normalises orientation via Exif metadata. If issues persist, verify the device is reporting rotation metadata correctly.
- **Firebase authentication errors** – Confirm the user exists in Firebase Authentication and that the device has network connectivity.

## Data Synchronisation

- **Records not uploading** – Check network connectivity and verify Firestore/Storage security rules allow the signed-in user.
- **Offline data missing after reinstall** – Local Room data is stored inside the app sandbox; uninstalling the app clears the database. Ensure background sync completed before uninstalling.

For additional support, review logcat output in Android Studio and cross-reference the Master Control Plan sections on Firebase and sync behaviour.
