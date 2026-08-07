package com.example.data.repository

import com.example.data.models.Announcement
import com.example.data.models.LocationLog
import com.example.data.models.PaymentInfo
import com.example.data.models.RouteState
import com.example.data.models.StudentStop
import com.example.data.models.UserProfile
import com.example.data.models.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class TransportRepository private constructor() {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var locationSimulationJob: Job? = null

    // Users
    private val _currentRole = MutableStateFlow(UserRole.DRIVER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    val driverUser = UserProfile(
        id = "driver_102",
        name = "Carlos Eduardo (Tio Carlos)",
        email = "carlos.van@escola.com",
        role = UserRole.DRIVER,
        vanIdentifier = "Perua Amarela #102 - Mercedes Sprinter"
    )

    val parentUser = UserProfile(
        id = "parent_501",
        name = "Maria Silva (Mãe do Lucas)",
        email = "maria.silva@email.com",
        role = UserRole.PARENT,
        vanIdentifier = "Perua Amarela #102 - Tio Carlos"
    )

    // Route State
    private val _routeState = MutableStateFlow(RouteState())
    val routeState: StateFlow<RouteState> = _routeState.asStateFlow()

    // Announcements
    private val _announcements = MutableStateFlow<List<Announcement>>(
        listOf(
            Announcement(
                id = "ann_1",
                driverId = "driver_102",
                driverName = "Tio Carlos",
                message = "Bom dia pais! Rota da manhã iniciada com sucesso. Previsão de chegada na escola às 07:35.",
                timestamp = System.currentTimeMillis() - 3600000, // 1 hour ago
                isUrgent = false
            ),
            Announcement(
                id = "ann_2",
                driverId = "driver_102",
                driverName = "Tio Carlos",
                message = "Lembrete: Amanhã haverá reunião de pais no Colégio Anchieta às 18h.",
                timestamp = System.currentTimeMillis() - 86400000, // 1 day ago
                isUrgent = false
            )
        )
    )
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    // Payment Info
    private val _paymentInfo = MutableStateFlow(PaymentInfo())
    val paymentInfo: StateFlow<PaymentInfo> = _paymentInfo.asStateFlow()

    // Pre-configured route waypoints (Simulated school van path around Paulista / Jardins area)
    private val waypoints = listOf(
        Pair(-23.5617, -46.6560), // Stop 1: Garagem Tio Carlos
        Pair(-23.5630, -46.6540), // Stop 2: Res. Lucas (Alameda Santos)
        Pair(-23.5652, -46.6518), // Stop 3: Res. Sophia & Pedro
        Pair(-23.5680, -46.6490), // Stop 4: Res. Enzo
        Pair(-23.5710, -46.6450), // Stop 5: Colégio Anchieta
        Pair(-23.5735, -46.6420)  // Stop 6: Escola Múltipla
    )

    // Student Stops
    private val _studentStops = MutableStateFlow<List<StudentStop>>(
        listOf(
            StudentStop("s1", "Lucas Silva", "Alameda Santos, 1200", -23.5630, -46.6540, isDone = false, isCurrentTarget = true),
            StudentStop("s2", "Sophia & Pedro", "Rua Augusta, 2400", -23.5652, -46.6518, isDone = false, isCurrentTarget = false),
            StudentStop("s3", "Enzo Gabriel", "Alameda Jaú, 850", -23.5680, -46.6490, isDone = false, isCurrentTarget = false),
            StudentStop("s4", "Colégio Anchieta (Escola)", "Rua Treze de Maio, 500", -23.5710, -46.6450, isDone = false, isCurrentTarget = false)
        )
    )
    val studentStops: StateFlow<List<StudentStop>> = _studentStops.asStateFlow()

    private var currentWaypointIndex = 0

    fun switchRole(role: UserRole) {
        _currentRole.value = role
    }

    fun startRoute() {
        if (_routeState.value.isActive) return

        currentWaypointIndex = 0
        val startLocation = waypoints[0]
        _routeState.value = RouteState(
            id = "route_${System.currentTimeMillis()}",
            driverId = driverUser.id,
            isActive = true,
            startedAt = System.currentTimeMillis(),
            currentSpeedKmH = 28f,
            headingDegrees = 110f,
            currentLocation = LocationLog(
                id = "loc_start",
                routeId = "route_active",
                latitude = startLocation.first,
                longitude = startLocation.second,
                heading = 110f,
                speed = 28f
            )
        )

        // Reset student stops
        _studentStops.value = _studentStops.value.mapIndexed { idx, stop ->
            stop.copy(isDone = false, isCurrentTarget = idx == 0)
        }

        startLocationSimulation()
    }

    fun stopRoute() {
        locationSimulationJob?.cancel()
        _routeState.value = _routeState.value.copy(
            isActive = false,
            endedAt = System.currentTimeMillis(),
            currentSpeedKmH = 0f
        )
    }

    fun updateLocationManually(lat: Double, lng: Double, speed: Float = 35f, heading: Float = 90f) {
        if (!_routeState.value.isActive) return
        val current = _routeState.value
        val log = LocationLog(
            id = "loc_${System.currentTimeMillis()}",
            routeId = current.id,
            latitude = lat,
            longitude = lng,
            heading = heading,
            speed = speed,
            timestamp = System.currentTimeMillis()
        )
        _routeState.value = current.copy(
            currentSpeedKmH = speed,
            headingDegrees = heading,
            currentLocation = log
        )
    }

    private fun startLocationSimulation() {
        locationSimulationJob?.cancel()
        locationSimulationJob = scope.launch {
            var stepProgress = 0f
            while (isActive && _routeState.value.isActive) {
                delay(4000) // 4 seconds update tick

                stepProgress += 0.25f
                val p1 = waypoints[currentWaypointIndex]
                val nextIndex = (currentWaypointIndex + 1) % waypoints.size
                val p2 = waypoints[nextIndex]

                if (stepProgress >= 1f) {
                    stepProgress = 0f
                    currentWaypointIndex = nextIndex
                    // Update student stops status
                    if (currentWaypointIndex < _studentStops.value.size) {
                        _studentStops.value = _studentStops.value.mapIndexed { idx, stop ->
                            when {
                                idx < currentWaypointIndex -> stop.copy(isDone = true, isCurrentTarget = false)
                                idx == currentWaypointIndex -> stop.copy(isDone = false, isCurrentTarget = true)
                                else -> stop.copy(isDone = false, isCurrentTarget = false)
                            }
                        }
                    }
                }

                val currentP1 = waypoints[currentWaypointIndex]
                val currentP2 = waypoints[(currentWaypointIndex + 1) % waypoints.size]

                val interpLat = currentP1.first + (currentP2.first - currentP1.first) * stepProgress
                val interpLng = currentP1.second + (currentP2.second - currentP1.second) * stepProgress

                val heading = calculateBearing(currentP1.first, currentP1.second, currentP2.first, currentP2.second)
                val simulatedSpeed = (25..42).random().toFloat()

                val log = LocationLog(
                    id = "loc_${System.currentTimeMillis()}",
                    routeId = _routeState.value.id,
                    latitude = interpLat,
                    longitude = interpLng,
                    heading = heading,
                    speed = simulatedSpeed,
                    timestamp = System.currentTimeMillis()
                )

                _routeState.value = _routeState.value.copy(
                    currentSpeedKmH = simulatedSpeed,
                    headingDegrees = heading,
                    currentLocation = log
                )

                // Envio das coordenadas para o Firebase Realtime Database
                FirebaseGpsRepository.getInstance().updateDriverLocation(
                    driverId = driverUser.id,
                    location = RealtimeLocationData(
                        latitude = interpLat,
                        longitude = interpLng,
                        speedKmh = simulatedSpeed,
                        bearing = heading,
                        status = "ACTIVE",
                        driverName = driverUser.name,
                        vanIdentifier = driverUser.vanIdentifier
                    )
                )
            }
        }
    }

    fun postAnnouncement(message: String, isUrgent: Boolean = false) {
        val newAnnouncement = Announcement(
            id = "ann_${System.currentTimeMillis()}",
            driverId = driverUser.id,
            driverName = driverUser.name.split(" ")[0] + " (Tio Carlos)",
            message = message,
            timestamp = System.currentTimeMillis(),
            isUrgent = isUrgent
        )
        _announcements.value = listOf(newAnnouncement) + _announcements.value
    }

    fun updatePaymentInfo(pixKey: String, pixKeyType: String, monthlyFee: Double) {
        _paymentInfo.value = _paymentInfo.value.copy(
            pixKey = pixKey,
            pixKeyType = pixKeyType,
            monthlyFee = monthlyFee
        )
    }

    private fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val dLonRad = Math.toRadians(lon2 - lon1)

        val y = sin(dLonRad) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLonRad)

        var bearing = Math.toDegrees(atan2(y, x)).toFloat()
        if (bearing < 0) bearing += 360f
        return bearing
    }

    companion object {
        @Volatile
        private var INSTANCE: TransportRepository? = null

        fun getInstance(): TransportRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TransportRepository().also { INSTANCE = it }
            }
        }
    }
}
