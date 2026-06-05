package com.example.spin_and_earn_money.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val usersCol = db.collection("users")
    private val referralCol = db.collection("referralCodes")
    private val withdrawalCol = db.collection("withdrawalRequests")

    private fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    /** Generate a random 7-character alphanumeric referral code */
    private fun generateReferralCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..7).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    /**
     * Fetches user document. If it does not exist, creates one with default values.
     * Also handles daily login bonus (only once per day).
     * Returns the final UserModel.
     */
    suspend fun getOrCreateUser(uid: String, name: String, email: String): UserModel {
        val docRef = usersCol.document(uid)
        val snapshot = docRef.get().await()

        return if (snapshot.exists()) {
            val user = snapshot.toObject(UserModel::class.java) ?: UserModel()
            val todayStr = today()

            // Daily login bonus — only if lastLoginDate != today
            if (user.lastLoginDate != todayStr) {
                val bonus = Random.nextLong(1000, 3001) // 1000–3000 points
                val updates = hashMapOf<String, Any>(
                    "lastLoginDate" to todayStr,
                    "points" to (user.points + bonus),
                    "todayEarning" to bonus,
                    "spinsToday" to 0,
                    "lastSpinDate" to todayStr
                )
                docRef.update(updates).await()
                user.copy(
                    lastLoginDate = todayStr,
                    points = user.points + bonus,
                    todayEarning = bonus,
                    spinsToday = 0,
                    lastSpinDate = todayStr
                )
            } else {
                user
            }
        } else {
            // New user
            val referralCode = generateReferralCode()
            val todayStr = today()
            val bonus = Random.nextLong(1000, 3001)

            val newUser = UserModel(
                uid = uid,
                name = name,
                email = email,
                points = bonus,
                totalEarnings = 0.0,
                todayEarning = bonus,
                referredBy = null,
                myReferralCode = referralCode,
                upiId = "",
                lastLoginDate = todayStr,
                spinsToday = 0,
                lastSpinDate = todayStr,
                referralCodeApplied = false
            )

            docRef.set(newUser).await()

            // Store referral code in referralCodes collection
            referralCol.document(referralCode).set(mapOf("uid" to uid)).await()

            newUser
        }
    }

    /**
     * Add points after a spin. Updates points, totalEarning, todayEarning,
     * spinsToday, and lastSpinDate in Firestore.
     */
    suspend fun addPointsAfterSpin(uid: String, pointsEarned: Long, currentUser: UserModel): UserModel {
        val todayStr = today()
        val newPoints = currentUser.points + pointsEarned
        val newTodayEarning = if (currentUser.lastSpinDate == todayStr)
            currentUser.todayEarning + pointsEarned else pointsEarned
        val newSpinsToday = if (currentUser.lastSpinDate == todayStr)
            currentUser.spinsToday + 1 else 1
        val newTotalEarnings = currentUser.totalEarnings + (pointsEarned.toDouble() / 1000.0)

        val updates = mapOf(
            "points" to newPoints,
            "todayEarning" to newTodayEarning,
            "spinsToday" to newSpinsToday,
            "lastSpinDate" to todayStr,
            "totalEarnings" to newTotalEarnings
        )
        usersCol.document(uid).update(updates).await()

        return currentUser.copy(
            points = newPoints,
            todayEarning = newTodayEarning,
            spinsToday = newSpinsToday,
            lastSpinDate = todayStr,
            totalEarnings = newTotalEarnings
        )
    }

    /**
     * Save a withdrawal request to Firestore.
     */
    suspend fun saveWithdrawalRequest(uid: String, upiId: String, amount: Int) {
        val request = hashMapOf(
            "uid" to uid,
            "upiId" to upiId,
            "amount" to amount,
            "status" to "pending",
            "timestamp" to com.google.firebase.Timestamp.now()
        )
        withdrawalCol.add(request).await()
    }

    /**
     * Deduct points for withdrawal and update upiId.
     */
    suspend fun deductPointsForWithdrawal(uid: String, pointsToDeduct: Long, upiId: String, currentUser: UserModel): UserModel {
        val newPoints = currentUser.points - pointsToDeduct
        usersCol.document(uid).update(
            mapOf(
                "points" to newPoints,
                "upiId" to upiId
            )
        ).await()
        return currentUser.copy(points = newPoints, upiId = upiId)
    }

    /**
     * Apply a referral code. Returns true if successful, false if code invalid/already used.
     * If successful, gives 2000 points to both users.
     */
    suspend fun applyReferralCode(currentUid: String, code: String, currentUser: UserModel): Result<UserModel> {
        if (currentUser.referralCodeApplied) {
            return Result.failure(Exception("Referral code already applied"))
        }

        val codeDoc = referralCol.document(code).get().await()
        if (!codeDoc.exists()) {
            return Result.failure(Exception("Invalid referral code"))
        }

        val referrerUid = codeDoc.getString("uid") ?: return Result.failure(Exception("Invalid referral code"))
        if (referrerUid == currentUid) {
            return Result.failure(Exception("Cannot use your own referral code"))
        }

        // Give 2000 points to current user
        val newPoints = currentUser.points + 2000
        usersCol.document(currentUid).update(
            mapOf(
                "points" to newPoints,
                "referredBy" to code,
                "referralCodeApplied" to true
            )
        ).await()

        // Give 2000 points to referrer
        val referrerDoc = usersCol.document(referrerUid).get().await()
        val referrerPoints = referrerDoc.getLong("points") ?: 0L
        usersCol.document(referrerUid).update("points", referrerPoints + 2000).await()

        return Result.success(
            currentUser.copy(
                points = newPoints,
                referredBy = code,
                referralCodeApplied = true
            )
        )
    }

    /**
     * Update the user's UPI ID in Firestore.
     */
    suspend fun updateUpiId(uid: String, upiId: String, currentUser: UserModel): UserModel {
        usersCol.document(uid).update("upiId", upiId).await()
        return currentUser.copy(upiId = upiId)
    }

    /**
     * Refresh user model from Firestore.
     */
    suspend fun refreshUser(uid: String): UserModel? {
        val doc = usersCol.document(uid).get().await()
        return if (doc.exists()) doc.toObject(UserModel::class.java) else null
    }
}
