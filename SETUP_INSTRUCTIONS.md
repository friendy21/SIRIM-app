# Firebase and Environment Setup

Follow these steps to connect the SIRIM OCR Android app to your Firebase project and prepare Android Studio for local development.

## 1. Firebase Project

1. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com).
2. Add an Android app with the package name `com.sirimocr.app` (or your custom applicationId).
3. Download the generated **google-services.json** file and replace the template located at `app/google-services.json`.
4. Enable the following Firebase products:
   - **Authentication**: turn on Email/Password sign-in.
   - **Cloud Firestore**: start in *Production* mode and select a region close to your users.
   - **Firebase Storage**: create the default storage bucket.
5. Update the Firebase security rules using the snippets provided in the Master Control Plan (README.md).

## 2. Android Studio Configuration

1. Install Android Studio Giraffe or newer with Android SDK Platform 34.
2. Clone this repository and open the project in Android Studio.
3. If the Gradle wrapper JAR is missing, run `gradle wrapper --gradle-version 8.2` once to regenerate it.
4. Sync the project so that the Google Services Gradle plugin recognises your `google-services.json` file.

## 3. Firebase Console Verification

1. From the Authentication tab, create a test user account.
2. In Cloud Firestore, create the `users/{uid}/records` collection path by adding a dummy document.
3. Upload a sample image to `users/{uid}/images/` inside Firebase Storage to verify permissions if needed.

## 4. Optional Local Tweaks

- Update `applicationId` and package namespaces in `app/build.gradle.kts` if you publish under a different ID.
- Change the colour palette in `app/src/main/res/values/colors.xml` to align with your branding.
- Review WorkManager sync intervals in `app/src/main/java/com/sirimocr/app/work/SyncManager.kt` to match your operational needs.

Once Firebase is connected, run the app on a physical device to test camera capture, offline storage, and synchronisation.
