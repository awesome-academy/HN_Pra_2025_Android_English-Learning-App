package com.sun.englishlearning.data.repository

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

interface AuthRepository {
    val currentUser: FirebaseUser?
    suspend fun signInWithGoogle(context: Context, serverClientId: String): Result<FirebaseUser>
    suspend fun signOut(context: Context): Result<Unit>
    suspend fun registerWithEmailPassword(email: String, password: String, displayName: String): Result<FirebaseUser>
    suspend fun signInWithEmailPassword(email: String, password: String): Result<FirebaseUser>
}

class AuthRepositoryImpl : AuthRepository {

    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override suspend fun signInWithGoogle(context: Context, serverClientId: String): Result<FirebaseUser> {
        val credentialManager = CredentialManager.create(context)

        // 1. Request Google ID Token from Credential Manager
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            val idToken = extractIdToken(credential)
                ?: return Result.failure(Exception("Cannot retrieve Google ID Token."))

            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            val user = authResult.user
            if (user != null) {
                // --- START OF NEW SECTION ---
                // 1. Create a Map containing user data
                val userProfile = hashMapOf(
                    "uid" to user.uid,
                    "displayName" to user.displayName,
                    "email" to user.email,
                    "photoUrl" to user.photoUrl.toString(),
                    "createdAt" to System.currentTimeMillis() // Save creation timestamp
                )

                // 2. Write data to Firestore
                // Use .document(user.uid) so the document ID matches the user's UID
                // Use set with SetOptions.merge() to:
                // - Create a new document if it doesn't exist.
                // - Update the document if it exists without overwriting other fields.
                db.collection("users").document(user.uid)
                    .set(userProfile, SetOptions.merge())
                    .await() // Use await to ensure the write operation completes

                // --- END OF NEW SECTION ---
                Result.success(user)
            } else {
                Result.failure(Exception("Firebase authentication failed."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractIdToken(credential: Credential): String? {
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            return try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                googleIdTokenCredential.idToken
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    override suspend fun signOut(context: Context): Result<Unit> {
        val credentialManager = CredentialManager.create(context)
        return try {
            auth.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun registerWithEmailPassword(email: String, password: String, displayName: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                // After successful registration, create user profile in Firestore
                val userProfile = hashMapOf(
                    "uid" to user.uid,
                    "displayName" to displayName,
                    "email" to user.email,
                    "createdAt" to System.currentTimeMillis()
                )
                db.collection("users").document(user.uid)
                    .set(userProfile, SetOptions.merge())
                    .await()
                Result.success(user)
            } else {
                Result.failure(Exception("Registration failed."))
            }
        } catch (e: Exception) {
            // This is the most important "email link" logic!
            if (e is FirebaseAuthUserCollisionException) {
                // Return a specific error for the Presenter to handle
                return Result.failure(Exception("EMAIL_COLLISION"))
            }
            Result.failure(e)
        }
    }

    override suspend fun signInWithEmailPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(authResult.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
