# Spin & Earn Flutter App

A complete Flutter-based **Spin & Earn Rewards App** integrated with **Firebase Authentication**, **Cloud Firestore**, and **Google Mobile Ads**. Users can log in using Google Sign-In, spin a reward wheel daily to earn points, refer friends for bonus rewards, and request withdrawals through UPI.

## ✨ Features

### 🔐 Authentication

* Google Authentication using Firebase Auth
* Automatic user registration in Firestore
* Persistent login session

### 👤 User Database (Firestore)

Each new user gets a Firestore document with:

```json
{
  "name": "",
  "email": "",
  "uid": "",
  "points": 0,
  "totalEarnings": 0,
  "todayEarning": 0,
  "referredBy": null,
  "myReferralCode": "",
  "upiId": "",
  "lastLoginDate": ""
}
```

### 🎡 Spin & Earn System

* Interactive spinning wheel with rewards:

  * 10 Points
  * 25 Points
  * 50 Points
  * 100 Points
* Maximum **5 spins per day**
* Daily spin reset using:

  * `spinsToday`
  * `lastSpinDate`
* Random rewards added to Firestore balance
* Rewarded Ads shown after every spin

### 💰 Wallet System

* Real-time point balance display
* Conversion:

  * **1000 Points = ₹1**
* Withdrawal request form
* Minimum withdrawal amount supported
* Withdrawal data stored in:
  `withdrawalRequests` collection

Example:

```json
{
  "uid": "",
  "upiId": "",
  "amount": 0,
  "status": "pending"
}
```

### 👥 Referral System

* Unique referral code generation for every user
* Referral codes stored in:
  `referralCodes` collection
* One-time referral code usage
* Both users receive referral bonus points

Referral Bonus:

* Referrer → +2000 Points
* New User → +2000 Points

### 🎁 Daily Login Bonus

* Random daily rewards:

  * 1000 to 3000 Points
* Reward granted only once per day
* Uses `lastLoginDate` to prevent duplicate claims

### 📢 Ad Integration

Google Mobile Ads integration:

* Rewarded Ads → Spin Screen
* Banner Ads → Wallet & Profile Screens
* Interstitial Ads → Between Screen Navigation

### 📱 Bottom Navigation

App contains 3 main tabs:

1. Spin
2. Wallet
3. Profile

### ☁️ Firestore Sync

* All user data synced in real-time
* Admin can manually verify withdrawals from Firestore

---

# 🛠️ Tech Stack

## Flutter Packages

```yaml
firebase_core
firebase_auth
cloud_firestore
google_sign_in
google_mobile_ads
provider
shared_preferences
```

---

# 📂 Firestore Collections

## users

Stores user profile and earnings data.

## referralCodes

```json
{
  "CODE123": {
    "uid": "user_uid"
  }
}
```

## withdrawalRequests

Stores withdrawal requests for admin processing.

---

# 🚀 Future Improvements

* Razorpay/Payout API integration
* Admin Panel
* Push Notifications
* Leaderboard System
* Spin Animation Enhancements
* Dark Mode Support

---

# 📌 Note

This project is intended for educational and learning purposes. Proper ad policy compliance and secure backend validation should be implemented before production deployment.
