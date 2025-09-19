package com.sirimocr.app.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AuthService {
    private val auth: FirebaseAuth = Firebase.auth
    private val firestore = Firebase.firestore

    fun currentUser(): FirebaseUser? = auth.currentUser

    suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return AuthResult.Error("Authentication failed")
            updateLastLogin(user.uid)
            AuthResult.Success(user)
        } catch (t: Throwable) {
            Log.e("AuthService", "Sign-in failed", t)
            AuthResult.Error(t.localizedMessage ?: "Unable to sign in")
        }
    }

    suspend fun register(email: String, password: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return AuthResult.Error("Registration failed")
            createUserProfile(user)
            AuthResult.Success(user)
        } catch (t: Throwable) {
            Log.e("AuthService", "Registration failed", t)
            AuthResult.Error(t.localizedMessage ?: "Unable to register")
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }

    private suspend fun createUserProfile(user: FirebaseUser) {
        val profile = hashMapOf(
            "uid" to user.uid,
            "email" to user.email,
            "displayName" to user.displayName,
            "createdAt" to FieldValue.serverTimestamp(),
            "lastLogin" to FieldValue.serverTimestamp(),
            "isActive" to true,
            "preferences" to hashMapOf(
                "autoSync" to true,
                "exportFormat" to "csv",
                "ocrEngine" to "mlkit"
            )
        )
        firestore.collection("users").document(user.uid).set(profile).await()
    }

    private suspend fun updateLastLogin(uid: String) {
        firestore.collection("users").document(uid)
            .update("lastLogin", FieldValue.serverTimestamp()).await()
    }
}

sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
