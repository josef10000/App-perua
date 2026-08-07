package com.example.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class RealtimeLocationData(
    val latitude: Double = -23.5617,
    val longitude: Double = -46.6560,
    val speedKmh: Float = 0.0f,
    val bearing: Float = 0.0f,
    val lastUpdated: Long = System.currentTimeMillis(),
    val status: String = "IDLE",
    val driverName: String = "Tio Carlos",
    val vanIdentifier: String = "Perua #102"
)

class FirebaseGpsRepository private constructor() {

    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }

    fun updateDriverLocation(driverId: String, location: RealtimeLocationData) {
        val ref = database.getReference("active_routes").child(driverId).child("location")
        val locationMap = hashMapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "speedKmh" to location.speedKmh,
            "bearing" to location.bearing,
            "lastUpdated" to System.currentTimeMillis(),
            "status" to location.status,
            "driverName" to location.driverName,
            "vanIdentifier" to location.vanIdentifier
        )
        ref.setValue(locationMap)
    }

    fun observeDriverLocation(driverId: String): Flow<RealtimeLocationData> = callbackFlow {
        val ref = database.getReference("active_routes").child(driverId).child("location")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val lat = snapshot.child("latitude").getValue(Double::class.java) ?: -23.5617
                    val lng = snapshot.child("longitude").getValue(Double::class.java) ?: -46.6560
                    val speed = snapshot.child("speedKmh").getValue(Float::class.java) ?: 0.0f
                    val bearing = snapshot.child("bearing").getValue(Float::class.java) ?: 0.0f
                    val updated = snapshot.child("lastUpdated").getValue(Long::class.java) ?: System.currentTimeMillis()
                    val status = snapshot.child("status").getValue(String::class.java) ?: "IDLE"
                    val driverName = snapshot.child("driverName").getValue(String::class.java) ?: "Tio Carlos"
                    val vanIdentifier = snapshot.child("vanIdentifier").getValue(String::class.java) ?: "Perua #102"

                    val data = RealtimeLocationData(
                        latitude = lat,
                        longitude = lng,
                        speedKmh = speed,
                        bearing = bearing,
                        lastUpdated = updated,
                        status = status,
                        driverName = driverName,
                        vanIdentifier = vanIdentifier
                    )
                    trySend(data)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun stopRouteTransmission(driverId: String) {
        val ref = database.getReference("active_routes").child(driverId).child("location")
        ref.child("status").setValue("STOPPED")
    }

    companion object {
        @Volatile
        private var INSTANCE: FirebaseGpsRepository? = null

        fun getInstance(): FirebaseGpsRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = FirebaseGpsRepository()
                INSTANCE = instance
                instance
            }
        }
    }
}
