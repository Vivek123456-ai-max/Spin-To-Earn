package com.example.spin_and_earn_money.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spin_and_earn_money.data.FirestoreRepository
import com.example.spin_and_earn_money.data.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class Success(val message: String) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

class ProfileViewModel : ViewModel() {

    private val repository = FirestoreRepository()

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> = _profileState

    fun applyReferralCode(
        currentUser: UserModel,
        code: String,
        onUserUpdate: (UserModel) -> Unit
    ) {
        if (code.isBlank()) {
            _profileState.value = ProfileState.Error("Please enter a referral code")
            return
        }
        if (currentUser.referralCodeApplied) {
            _profileState.value = ProfileState.Error("You have already applied a referral code")
            return
        }

        _profileState.value = ProfileState.Loading

        viewModelScope.launch {
            val result = repository.applyReferralCode(currentUser.uid, code.trim().uppercase(), currentUser)
            result.fold(
                onSuccess = { updatedUser ->
                    onUserUpdate(updatedUser)
                    _profileState.value = ProfileState.Success("Referral applied! You both received 2000 points 🎉")
                },
                onFailure = { e ->
                    _profileState.value = ProfileState.Error(e.message ?: "Failed to apply referral code")
                }
            )
        }
    }

    fun updateUpiId(
        currentUser: UserModel,
        upiId: String,
        onUserUpdate: (UserModel) -> Unit
    ) {
        if (upiId.isBlank() || !upiId.contains("@")) {
            _profileState.value = ProfileState.Error("Please enter a valid UPI ID (e.g. name@upi)")
            return
        }

        _profileState.value = ProfileState.Loading

        viewModelScope.launch {
            try {
                val updatedUser = repository.updateUpiId(currentUser.uid, upiId.trim(), currentUser)
                onUserUpdate(updatedUser)
                _profileState.value = ProfileState.Success("UPI ID updated successfully!")
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Failed to update UPI ID")
            }
        }
    }

    fun resetState() {
        _profileState.value = ProfileState.Idle
    }
}
