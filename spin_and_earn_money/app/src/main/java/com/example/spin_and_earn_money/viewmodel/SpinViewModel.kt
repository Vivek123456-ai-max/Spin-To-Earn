package com.example.spin_and_earn_money.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spin_and_earn_money.data.FirestoreRepository
import com.example.spin_and_earn_money.data.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed class SpinState {
    object Idle : SpinState()
    object Spinning : SpinState()
    data class Done(val points: Long, val segmentIndex: Int) : SpinState()
    data class Error(val message: String) : SpinState()
    object MaxSpinsReached : SpinState()
    object ShowAd : SpinState()
}

class SpinViewModel : ViewModel() {

    private val repository = FirestoreRepository()

    // The reward slots on the wheel
    val rewards = listOf(10L, 25L, 50L, 10L, 100L, 25L, 50L, 10L)

    private val _spinState = MutableStateFlow<SpinState>(SpinState.Idle)
    val spinState: StateFlow<SpinState> = _spinState

    private val _targetAngle = MutableStateFlow(0f)
    val targetAngle: StateFlow<Float> = _targetAngle

    fun spin(currentUser: UserModel, onUserUpdate: (UserModel) -> Unit) {
        // Check max 5 spins per day
        val spinsToday = currentUser.spinsToday
        if (spinsToday >= 5) {
            _spinState.value = SpinState.MaxSpinsReached
            return
        }

        _spinState.value = SpinState.Spinning

        // Pick a random segment
        val segmentIndex = Random.nextInt(rewards.size)
        val segmentAngle = 360f / rewards.size
        // Calculate the target rotation: multiple full rotations + landing angle
        val extraRotations = (5 + Random.nextInt(5)) * 360f
        val landingAngle = 360f - (segmentIndex * segmentAngle + segmentAngle / 2)
        _targetAngle.value = extraRotations + landingAngle

        // After spin animation completes (called from UI), show ad
        _spinState.value = SpinState.ShowAd

        // Store the segment so we can apply points after ad
        pendingSegmentIndex = segmentIndex
        pendingUser = currentUser
        pendingOnUpdate = onUserUpdate
    }

    private var pendingSegmentIndex = 0
    private var pendingUser: UserModel? = null
    private var pendingOnUpdate: ((UserModel) -> Unit)? = null

    fun onAdWatched() {
        val user = pendingUser ?: return
        val segIdx = pendingSegmentIndex
        val pointsEarned = rewards[segIdx]

        viewModelScope.launch {
            try {
                val updatedUser = repository.addPointsAfterSpin(user.uid, pointsEarned, user)
                pendingOnUpdate?.invoke(updatedUser)
                _spinState.value = SpinState.Done(pointsEarned, segIdx)
            } catch (e: Exception) {
                _spinState.value = SpinState.Error(e.message ?: "Failed to update points")
            }
        }
    }

    fun onAdSkipped() {
        // Still give points even if ad is skipped (optional policy)
        onAdWatched()
    }

    fun resetState() {
        _spinState.value = SpinState.Idle
    }
}
