package com.example.spin_and_earn_money.data

import com.google.firebase.Timestamp

data class WithdrawalRequest(
    val id: String = "",
    val uid: String = "",
    val upiId: String = "",
    val amount: Int = 0,
    val status: String = "pending",
    val timestamp: Timestamp? = null
)
