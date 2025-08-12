// data/repository/CategoryRepository.kt
package com.sun.englishlearning.data.repository

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sun.englishlearning.data.model.Category
import kotlinx.coroutines.tasks.await

interface CategoryRepository {
    suspend fun getCategories(): Result<List<Category>>
}

class CategoryRepositoryImpl : CategoryRepository {
    private val db = Firebase.firestore

    override suspend fun getCategories(): Result<List<Category>> {
        return try {
            val snapshot = db.collection("categories")
                .orderBy("order")
                .get()
                .await()

            // Convert the received documents into a list of Category objects
            val categories = snapshot.documents.mapNotNull { document ->
                // Use toObject to automatically map, and assign id to the document
                document.toObject(Category::class.java)?.copy(id = document.id)
            }
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
