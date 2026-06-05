package com.example.spin_and_earn_money.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spin_and_earn_money.ads.BannerAdView
import com.example.spin_and_earn_money.data.UserModel
import com.example.spin_and_earn_money.viewmodel.AuthViewModel
import com.example.spin_and_earn_money.viewmodel.ProfileState
import com.example.spin_and_earn_money.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    authViewModel: AuthViewModel,
    currentUser: UserModel,
    onUserUpdate: (UserModel) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity
    val profileState by viewModel.profileState.collectAsState()

    var showEditUpiDialog by remember { mutableStateOf(false) }
    var showReferralDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var upiInput by remember(currentUser.upiId) { mutableStateOf(currentUser.upiId) }
    var referralInput by remember { mutableStateOf("") }

    LaunchedEffect(profileState) {
        when (val state = profileState) {
            is ProfileState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                showEditUpiDialog = false
                showReferralDialog = false
                viewModel.resetState()
            }
            is ProfileState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(DarkBg, Color(0xFF1A0533), DarkBg)))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(
                    Brush.radialGradient(colors = listOf(DeepPurple, AccentPink)),
                    shape = CircleShape
                )
                .border(3.dp, VibrantGold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = TextWhite,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(currentUser.name, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
        Text(currentUser.email, fontSize = 13.sp, color = TextGray)

        Spacer(modifier = Modifier.height(8.dp))

        // Points badge
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DeepPurple.copy(alpha = 0.3f))
        ) {
            Text(
                text = "⭐ ${currentUser.points} Points",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = VibrantGold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Info Cards
        ProfileInfoCard(
            title = "📞 UPI ID",
            value = currentUser.upiId.ifBlank { "Not set" },
            actionLabel = "Edit",
            onAction = { showEditUpiDialog = true }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Referral Code Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🎁 Your Referral Code", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(listOf(DeepPurple.copy(0.3f), AccentPink.copy(0.2f))),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = currentUser.myReferralCode,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = VibrantGold,
                            letterSpacing = 4.sp
                        )
                    }
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Referral Code", currentUser.myReferralCode))
                            Toast.makeText(context, "Referral code copied!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextGray)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Share this code with friends. Both of you get 2000 points! 🎉",
                    fontSize = 12.sp,
                    color = TextGray
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Apply Referral Code Card
        if (!currentUser.referralCodeApplied) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🔗 Apply a Referral Code", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Enter a friend's referral code to earn 2000 bonus points!",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showReferralDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Enter Referral Code", color = DarkBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✅", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Referral Applied!", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GreenAccent)
                        if (!currentUser.referredBy.isNullOrBlank()) {
                            Text(
                                "Referred by: ${currentUser.referredBy}",
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Stats Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📊 Your Stats", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem("💰", "Points", "${currentUser.points}")
                    StatItem("📅", "Today", "+${currentUser.todayEarning}")
                    StatItem("🏦", "Total ₹", "%.2f".format(currentUser.totalEarnings))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Logout Button
        OutlinedButton(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF6B6B)),
            border = BorderStroke(1.dp, Color(0xFFFF6B6B))
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Banner Ad
        BannerAdView()
    }

    // Edit UPI Dialog
    if (showEditUpiDialog) {
        AlertDialog(
            onDismissRequest = { showEditUpiDialog = false },
            containerColor = CardBg,
            shape = RoundedCornerShape(24.dp),
            title = { Text("✏️ Edit UPI ID", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = upiInput,
                    onValueChange = { upiInput = it },
                    label = { Text("UPI ID", color = TextGray) },
                    placeholder = { Text("e.g. name@paytm", color = TextGray.copy(0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeepPurple,
                        unfocusedBorderColor = TextGray.copy(0.3f),
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        cursorColor = DeepPurple
                    ),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateUpiId(currentUser, upiInput.trim(), onUserUpdate)
                    },
                    enabled = profileState !is ProfileState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepPurple),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (profileState is ProfileState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = TextWhite, strokeWidth = 2.dp)
                    } else {
                        Text("Save", color = TextWhite)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditUpiDialog = false }) {
                    Text("Cancel", color = TextGray)
                }
            }
        )
    }

    // Referral Code Dialog
    if (showReferralDialog) {
        AlertDialog(
            onDismissRequest = { showReferralDialog = false },
            containerColor = CardBg,
            shape = RoundedCornerShape(24.dp),
            title = { Text("🎁 Apply Referral Code", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter a friend's referral code to earn 2000 bonus points each!", fontSize = 13.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = referralInput,
                        onValueChange = { referralInput = it.uppercase() },
                        label = { Text("Referral Code", color = TextGray) },
                        placeholder = { Text("e.g. ABCD123", color = TextGray.copy(0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeepPurple,
                            unfocusedBorderColor = TextGray.copy(0.3f),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            cursorColor = DeepPurple
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.applyReferralCode(currentUser, referralInput.trim(), onUserUpdate)
                    },
                    enabled = profileState !is ProfileState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (profileState is ProfileState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = DarkBg, strokeWidth = 2.dp)
                    } else {
                        Text("Apply Code", color = DarkBg, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showReferralDialog = false }) {
                    Text("Cancel", color = TextGray)
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = CardBg,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Sign Out?", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to sign out?", color = TextGray) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.signOut(activity)
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4444))
                ) {
                    Text("Sign Out", color = TextWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = TextGray)
                }
            }
        )
    }
}

@Composable
private fun ProfileInfoCard(title: String, value: String, actionLabel: String, onAction: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 12.sp, color = TextGray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
            }
            IconButton(onClick = onAction) {
                Icon(Icons.Default.Edit, contentDescription = actionLabel, tint = DeepPurple)
            }
        }
    }
}

@Composable
private fun StatItem(emoji: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
        Text(label, fontSize = 11.sp, color = TextGray)
    }
}
