package com.example.spin_and_earn_money.viewmodel

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spin_and_earn_money.data.FirestoreRepository
import com.example.spin_and_earn_money.data.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: UserModel) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val repository = FirestoreRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<UserModel?>(null)
    val currentUser: StateFlow<UserModel?> = _currentUser

    private var userListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun getGoogleSignInClient(activity: Activity): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("421098830293-qar8dnle3q25r1v32h7eku2chpuvo77s.apps.googleusercontent.com")
            .requestEmail()
            .requestProfile()
            .build()
        return GoogleSignIn.getClient(activity, gso)
    }

    fun handleSignInResult(data: Intent?) {
        _authState.value = AuthState.Loading
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            auth.signInWithCredential(credential)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        if (firebaseUser != null) {
                            viewModelScope.launch {
                                try {
                                    val user = repository.getOrCreateUser(
                                        uid = firebaseUser.uid,
                                        name = firebaseUser.displayName ?: "User",
                                        email = firebaseUser.email ?: ""
                                    )
                                    _currentUser.value = user
                                    _authState.value = AuthState.Success(user)
                                    startUserRealtimeListener(firebaseUser.uid)
                                } catch (e: Exception) {
                                    _authState.value = AuthState.Error(e.message ?: "Firestore error")
                                }
                            }
                        }
                    } else {
                        _authState.value = AuthState.Error(authTask.exception?.message ?: "Authentication failed")
                    }
                }
        } catch (e: ApiException) {
            _authState.value = AuthState.Error("Google Sign-In failed: ${e.message}")
        }
    }

    fun checkExistingLogin() {
        val firebaseUser = auth.currentUser ?: return
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val user = repository.getOrCreateUser(
                    uid = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "User",
                    email = firebaseUser.email ?: ""
                )
                _currentUser.value = user
                _authState.value = AuthState.Success(user)
                startUserRealtimeListener(firebaseUser.uid)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error loading user")
            }
        }
    }

    fun updateCurrentUser(user: UserModel) {
        _currentUser.value = user
    }

    fun signOut(activity: Activity) {
        userListenerRegistration?.remove()
        userListenerRegistration = null
        auth.signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("421098830293-qar8dnle3q25r1v32h7eku2chpuvo77s.apps.googleusercontent.com")
            .requestEmail()
            .build()
        GoogleSignIn.getClient(activity, gso).signOut()
        _authState.value = AuthState.Idle
        _currentUser.value = null
    }

    private fun startUserRealtimeListener(uid: String) {
        userListenerRegistration?.remove()
        userListenerRegistration = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(UserModel::class.java)
                    if (user != null) {
                        _currentUser.value = user
                        if (_authState.value is AuthState.Success) {
                            _authState.value = AuthState.Success(user)
                        }
                    }
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        userListenerRegistration?.remove()
        userListenerRegistration = null
    }
}
