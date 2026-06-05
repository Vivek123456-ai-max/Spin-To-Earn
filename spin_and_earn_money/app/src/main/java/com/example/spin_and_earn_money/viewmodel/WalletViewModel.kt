package com.example.spin_and_earn_money.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spin_and_earn_money.data.FirestoreRepository
import com.example.spin_and_earn_money.data.UserModel
import com.example.spin_and_earn_money.data.WithdrawalRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class WalletState {
    object Idle : WalletState()
    object Loading : WalletState()
    object Success : WalletState()
    data class Error(val message: String) : WalletState()
}

class WalletViewModel : ViewModel() {

    private val repository = FirestoreRepository()

    private val _walletState = MutableStateFlow<WalletState>(WalletState.Idle)
    val walletState: StateFlow<WalletState> = _walletState

    private val _withdrawals = MutableStateFlow<List<WithdrawalRequest>>(emptyList())
    val withdrawals: StateFlow<List<WithdrawalRequest>> = _withdrawals

    private var withdrawalsListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    fun startWithdrawalsListener(uid: String) {
        withdrawalsListenerRegistration?.remove()
        withdrawalsListenerRegistration = FirebaseFirestore.getInstance()
            .collection("withdrawalRequests")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val list = snapshot?.documents?.mapNotNull { doc ->
                    val req = doc.toObject(WithdrawalRequest::class.java)
                    req?.copy(id = doc.id)
                }?.sortedByDescending { it.timestamp?.seconds ?: 0L } ?: emptyList()
                _withdrawals.value = list
            }
    }

    /**
     * Submit a withdrawal request.
     * Minimum amount: 100 rupees (= 100,000 points at 1000pts = ₹1).
     * Actually user enters rupee amount, minimum ₹100 = 100,000 points.
     * 1000 points = ₹1, so ₹100 = 100,000 points.
     */
    fun submitWithdrawal(
        currentUser: UserModel,
        upiId: String,
        amountInRupees: Int,
        onUserUpdate: (UserModel) -> Unit
    ) {
        val pointsNeeded = amountInRupees * 1000L
        if (amountInRupees < 100) {
            _walletState.value = WalletState.Error("Minimum withdrawal is ₹100")
            return
        }
        if (currentUser.points < pointsNeeded) {
            _walletState.value = WalletState.Error("Insufficient points. You need ${pointsNeeded} points for ₹${amountInRupees}")
            return
        }
        if (upiId.isBlank()) {
            _walletState.value = WalletState.Error("Please enter a valid UPI ID")
            return
        }

        _walletState.value = WalletState.Loading

        viewModelScope.launch {
            try {
                repository.saveWithdrawalRequest(currentUser.uid, upiId, amountInRupees)
                val updatedUser = repository.deductPointsForWithdrawal(
                    uid = currentUser.uid,
                    pointsToDeduct = pointsNeeded,
                    upiId = upiId,
                    currentUser = currentUser
                )
                onUserUpdate(updatedUser)
                _walletState.value = WalletState.Success
            } catch (e: Exception) {
                _walletState.value = WalletState.Error(e.message ?: "Withdrawal failed")
            }
        }
    }

    fun resetState() {
        _walletState.value = WalletState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        withdrawalsListenerRegistration?.remove()
        withdrawalsListenerRegistration = null
    }
}
