package com.example.data.models

enum class UserRole {
    DRIVER,
    PARENT
}

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val vanIdentifier: String = "Perua Amarela - Tio Carlos (#102)"
)

data class LocationLog(
    val id: String,
    val routeId: String,
    val latitude: Double,
    val longitude: Double,
    val heading: Float, // Direction in degrees (0-360)
    val speed: Float,   // Speed in km/h
    val timestamp: Long = System.currentTimeMillis()
)

data class RouteState(
    val id: String = "route_active_001",
    val driverId: String = "driver_carlos",
    val isActive: Boolean = false,
    val startedAt: Long? = null,
    val endedAt: Long? = null,
    val currentSpeedKmH: Float = 0f,
    val headingDegrees: Float = 45f,
    val currentLocation: LocationLog = LocationLog(
        id = "loc_0",
        routeId = "route_active_001",
        latitude = -23.550520, // São Paulo center reference
        longitude = -46.633308,
        heading = 45f,
        speed = 0f
    )
)

data class Announcement(
    val id: String,
    val driverId: String,
    val driverName: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isUrgent: Boolean = false
)

data class PaymentInfo(
    val id: String = "pay_001",
    val driverId: String = "driver_carlos",
    val driverName: String = "Carlos Eduardo (Tio Carlos)",
    val pixKey: String = "tiocarlos.transporte@gmail.com",
    val pixKeyType: String = "E-mail", // E-mail, CPF, Telefone, Chave Aleatória
    val monthlyFee: Double = 380.00,
    val studentName: String = "Lucas Silva (3º Ano - Colégio Anchieta)"
)

data class StudentStop(
    val id: String,
    val studentName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isDone: Boolean = false,
    val isCurrentTarget: Boolean = false
)
