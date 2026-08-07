package com.example.data.repository

import com.example.data.models.Announcement
import com.example.data.models.StudentStop
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseFirestoreRepository private constructor() {

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun postAnnouncement(
        driverId: String,
        driverName: String,
        message: String,
        isUrgent: Boolean
    ): Boolean {
        val store = firestore ?: return false
        return try {
            val docRef = store.collection("announcements").document()
            val announcementMap = hashMapOf(
                "id" to docRef.id,
                "driverId" to driverId,
                "driverName" to driverName,
                "message" to message,
                "timestamp" to System.currentTimeMillis(),
                "isUrgent" to isUrgent
            )
            docRef.set(announcementMap).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun observeAnnouncements(): Flow<List<Announcement>> = callbackFlow {
        val store = firestore
        if (store == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        try {
            val query = store.collection("announcements")
                .orderBy("timestamp", Query.Direction.DESCENDING)

            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val id = doc.getString("id") ?: doc.id
                        val driverId = doc.getString("driverId") ?: ""
                        val driverName = doc.getString("driverName") ?: "Motorista"
                        val message = doc.getString("message") ?: ""
                        val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                        val isUrgent = doc.getBoolean("isUrgent") ?: false

                        Announcement(
                            id = id,
                            driverId = driverId,
                            driverName = driverName,
                            message = message,
                            timestamp = timestamp,
                            isUrgent = isUrgent
                        )
                    }
                    trySend(list)
                }
            }

            awaitClose { listener.remove() }
        } catch (e: Exception) {
            trySend(emptyList())
            close()
        }
    }

    suspend fun updateStudentStop(stop: StudentStop) {
        val store = firestore ?: return
        try {
            val stopMap = hashMapOf(
                "id" to stop.id,
                "studentName" to stop.studentName,
                "address" to stop.address,
                "lat" to stop.latitude,
                "lng" to stop.longitude,
                "isDone" to stop.isDone,
                "isCurrentTarget" to stop.isCurrentTarget
            )
            store.collection("students").document(stop.id).set(stopMap).await()
        } catch (e: Exception) {
            // Log exception
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: FirebaseFirestoreRepository? = null

        fun getInstance(): FirebaseFirestoreRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = FirebaseFirestoreRepository()
                INSTANCE = instance
                instance
            }
        }
    }
}
