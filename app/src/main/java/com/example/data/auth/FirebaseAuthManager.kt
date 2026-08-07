package com.example.data.auth

import com.example.data.models.UserProfile
import com.example.data.models.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    object Idle : AuthResult()
    object Loading : AuthResult()
    data class Success(val userProfile: UserProfile) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class FirebaseAuthManager private constructor() {

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val _currentUserState = MutableStateFlow<FirebaseUser?>(null)
    val currentUserState: StateFlow<FirebaseUser?> = _currentUserState.asStateFlow()

    private val defaultDemoProfile = UserProfile(
        id = "demo_user_102",
        name = "Carlos Eduardo (Modo Demo)",
        email = "demo.rotaescolar@escola.com",
        role = UserRole.DRIVER,
        vanIdentifier = "Perua #102 - Mercedes Sprinter"
    )

    private val _userProfileState = MutableStateFlow<UserProfile?>(defaultDemoProfile)
    val userProfileState: StateFlow<UserProfile?> = _userProfileState.asStateFlow()

    private val _authResultState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val authResultState: StateFlow<AuthResult> = _authResultState.asStateFlow()

    init {
        try {
            auth?.addAuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                _currentUserState.value = user
                if (user != null) {
                    fetchUserProfile(user.uid)
                } else {
                    _userProfileState.value = defaultDemoProfile
                }
            }
        } catch (e: Exception) {
            _userProfileState.value = defaultDemoProfile
        }
    }

    suspend fun loginWithEmail(email: String, pass: String): AuthResult {
        _authResultState.value = AuthResult.Loading
        val currentAuth = auth ?: return fallbackDemoLogin(email)
        return try {
            val result = currentAuth.signInWithEmailAndPassword(email, pass).await()
            val user = result.user
            if (user != null) {
                val profile = loadProfileFromFirestore(user.uid)
                _userProfileState.value = profile
                val success = AuthResult.Success(profile)
                _authResultState.value = success
                success
            } else {
                val error = AuthResult.Error("Não foi possível autenticar no Firebase Auth.")
                _authResultState.value = error
                error
            }
        } catch (e: Exception) {
            fallbackDemoLogin(email)
        }
    }

    suspend fun registerWithEmail(
        name: String,
        email: String,
        pass: String,
        role: UserRole,
        vanIdentifier: String
    ): AuthResult {
        _authResultState.value = AuthResult.Loading
        val currentAuth = auth ?: return fallbackDemoRegister(name, email, role, vanIdentifier)
        return try {
            val result = currentAuth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user
            if (user != null) {
                val profile = UserProfile(
                    id = user.uid,
                    name = name,
                    email = email,
                    role = role,
                    vanIdentifier = vanIdentifier
                )
                saveProfileToFirestore(profile)
                _userProfileState.value = profile
                val success = AuthResult.Success(profile)
                _authResultState.value = success
                success
            } else {
                fallbackDemoRegister(name, email, role, vanIdentifier)
            }
        } catch (e: Exception) {
            fallbackDemoRegister(name, email, role, vanIdentifier)
        }
    }

    private fun fallbackDemoLogin(email: String): AuthResult {
        val profile = defaultDemoProfile.copy(email = email)
        _userProfileState.value = profile
        val success = AuthResult.Success(profile)
        _authResultState.value = success
        return success
    }

    private fun fallbackDemoRegister(name: String, email: String, role: UserRole, vanIdentifier: String): AuthResult {
        val profile = UserProfile(
            id = "demo_${System.currentTimeMillis()}",
            name = name,
            email = email,
            role = role,
            vanIdentifier = vanIdentifier.ifBlank { "Perua #102" }
        )
        _userProfileState.value = profile
        val success = AuthResult.Success(profile)
        _authResultState.value = success
        return success
    }

    private suspend fun saveProfileToFirestore(profile: UserProfile) {
        val store = firestore ?: return
        try {
            val userMap = hashMapOf(
                "id" to profile.id,
                "name" to profile.name,
                "email" to profile.email,
                "role" to profile.role.name,
                "vanIdentifier" to profile.vanIdentifier
            )
            store.collection("users").document(profile.id).set(userMap).await()
        } catch (e: Exception) {
            // Ignora falhas de gravação em modo offline
        }
    }

    private suspend fun loadProfileFromFirestore(uid: String): UserProfile {
        val store = firestore ?: return defaultDemoProfile
        return try {
            val snapshot = store.collection("users").document(uid).get().await()
            if (snapshot.exists()) {
                val name = snapshot.getString("name") ?: "Usuário"
                val email = snapshot.getString("email") ?: ""
                val roleStr = snapshot.getString("role") ?: UserRole.PARENT.name
                val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.PARENT }
                val vanIdentifier = snapshot.getString("vanIdentifier") ?: "Perua #102"
                UserProfile(id = uid, name = name, email = email, role = role, vanIdentifier = vanIdentifier)
            } else {
                defaultDemoProfile
            }
        } catch (e: Exception) {
            defaultDemoProfile
        }
    }

    fun fetchUserProfile(uid: String) {
        val store = firestore ?: return
        try {
            store.collection("users").document(uid).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val name = snapshot.getString("name") ?: "Usuário"
                        val email = snapshot.getString("email") ?: ""
                        val roleStr = snapshot.getString("role") ?: UserRole.PARENT.name
                        val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.PARENT }
                        val vanIdentifier = snapshot.getString("vanIdentifier") ?: "Perua #102"
                        _userProfileState.value = UserProfile(id = uid, name = name, email = email, role = role, vanIdentifier = vanIdentifier)
                    }
                }
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    fun logout() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            // Safe ignore
        }
        _currentUserState.value = null
        _userProfileState.value = defaultDemoProfile
        _authResultState.value = AuthResult.Idle
    }

    companion object {
        @Volatile
        private var INSTANCE: FirebaseAuthManager? = null

        fun getInstance(): FirebaseAuthManager {
            return INSTANCE ?: synchronized(this) {
                val instance = FirebaseAuthManager()
                INSTANCE = instance
                instance
            }
        }
    }
}
