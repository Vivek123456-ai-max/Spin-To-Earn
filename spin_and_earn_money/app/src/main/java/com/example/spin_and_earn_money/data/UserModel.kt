package com.example.spin_and_earn_money.data

data class UserModel(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val points: Long = 0L,
    val totalEarnings: Double = 0.0,
    val todayEarning: Long = 0L,
    val referredBy: String? = null,
    val myReferralCode: String = "",
    val upiId: String = "",
    val lastLoginDate: String = "",
    val spinsToday: Int = 0,
    val lastSpinDate: String = "",
    val referralCodeApplied: Boolean = false
)
