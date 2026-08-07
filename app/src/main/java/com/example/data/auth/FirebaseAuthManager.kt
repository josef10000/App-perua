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

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val _currentUserState = MutableStateFlow<FirebaseUser?>(null)
    val currentUserState: StateFlow<FirebaseUser?> = _currentUserState.asStateFlow()

    private val _userProfileState = MutableStateFlow<UserProfile?>(null)
    val userProfileState: StateFlow<UserProfile?> = _userProfileState.asStateFlow()

    private val _authResultState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val authResultState: StateFlow<AuthResult> = _authResultState.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentUserState.value = user
            if (user != null) {
                fetchUserProfile(user.uid)
            } else {
                _userProfileState.value = null
            }
        }
    }

    suspend fun loginWithEmail(email: String, pass: String): AuthResult {
        _authResultState.value = AuthResult.Loading
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val user = result.user
            if (user != null) {
                val profile = loadProfileFromFirestore(user.uid)
                _userProfileState.value = profile
                val success = AuthResult.Success(profile)
                _authResultState.value = success
                success
            } else {
                val error = AuthResult.Error("Não foi possível autenticar o usuário.")
                _authResultState.value = error
                error
            }
        } catch (e: Exception) {
            val error = AuthResult.Error(e.localizedMessage ?: "Erro ao realizar login.")
            _authResultState.value = error
            error
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
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
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
                val error = AuthResult.Error("Erro ao criar conta no Firebase Auth.")
                _authResultState.value = error
                error
            }
        } catch (e: Exception) {
            val error = AuthResult.Error(e.localizedMessage ?: "Erro ao cadastrar usuário.")
            _authResultState.value = error
            error
        }
    }

    private suspend fun saveProfileToFirestore(profile: UserProfile) {
        val userMap = hashMapOf(
            "id" to profile.id,
            "name" to profile.name,
            "email" to profile.email,
            "role" to profile.role.name,
            "vanIdentifier" to profile.vanIdentifier
        )
        firestore.collection("users").document(profile.id).set(userMap).await()
    }

    private suspend fun loadProfileFromFirestore(uid: String): UserProfile {
        return try {
            val snapshot = firestore.collection("users").document(uid).get().await()
            if (snapshot.exists()) {
                val name = snapshot.getString("name") ?: "Usuário"
                val email = snapshot.getString("email") ?: ""
                val roleStr = snapshot.getString("role") ?: UserRole.PARENT.name
                val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.PARENT }
                val vanIdentifier = snapshot.getString("vanIdentifier") ?: "Perua #102"
                UserProfile(id = uid, name = name, email = email, role = role, vanIdentifier = vanIdentifier)
            } else {
                UserProfile(id = uid, name = "Usuário", email = "", role = UserRole.PARENT, vanIdentifier = "Perua #102")
            }
        } catch (e: Exception) {
            UserProfile(id = uid, name = "Usuário", email = "", role = UserRole.PARENT, vanIdentifier = "Perua #102")
        }
    }

    fun fetchUserProfile(uid: String) {
        firestore.collection("users").document(uid).get()
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
    }

    fun logout() {
        auth.signOut()
        _currentUserState.value = null
        _userProfileState.value = null
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
