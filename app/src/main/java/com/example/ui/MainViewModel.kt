package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.Announcement
import com.example.data.models.PaymentInfo
import com.example.data.models.RouteState
import com.example.data.models.StudentStop
import com.example.data.models.UserRole
import com.example.data.repository.TransportRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainViewModel : ViewModel() {

    private val repository = TransportRepository.getInstance()

    val currentRole: StateFlow<UserRole> = repository.currentRole.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserRole.DRIVER
    )

    val routeState: StateFlow<RouteState> = repository.routeState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RouteState()
    )

    val announcements: StateFlow<List<Announcement>> = repository.announcements.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val paymentInfo: StateFlow<PaymentInfo> = repository.paymentInfo.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PaymentInfo()
    )

    val studentStops: StateFlow<List<StudentStop>> = repository.studentStops.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val driverProfile = repository.driverUser
    val parentProfile = repository.parentUser

    fun switchRole(role: UserRole) {
        repository.switchRole(role)
    }

    fun startRoute() {
        repository.startRoute()
    }

    fun stopRoute() {
        repository.stopRoute()
    }

    fun postAnnouncement(message: String, isUrgent: Boolean = false) {
        repository.postAnnouncement(message, isUrgent)
    }

    fun updatePaymentInfo(pixKey: String, pixKeyType: String, monthlyFee: Double) {
        repository.updatePaymentInfo(pixKey, pixKeyType, monthlyFee)
    }

    fun updateManualCoordinates(lat: Double, lng: Double) {
        repository.updateLocationManually(lat, lng)
    }

    fun addStudentStop(name: String, address: String) {
        repository.addStudentStop(name, address)
    }
}
