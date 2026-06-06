package com.example.spin_and_earn_money.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spin_and_earn_money.ads.BannerAdView
import com.example.spin_and_earn_money.data.UserModel
import androidx.compose.foundation.clickable
import com.example.spin_and_earn_money.data.WithdrawalRequest
import com.example.spin_and_earn_money.viewmodel.WalletState
import com.example.spin_and_earn_money.viewmodel.WalletViewModel

@Composable
fun WalletScreen(
    viewModel: WalletViewModel,
    currentUser: UserModel,
    onUserUpdate: (UserModel) -> Unit
) {
    val walletState by viewModel.walletState.collectAsState()
    val withdrawals by viewModel.withdrawals.collectAsState()

    var upiId             by remember(currentUser.upiId) { mutableStateOf(currentUser.upiId) }
    var amountText        by remember { mutableStateOf("") }
    var showWithdrawSheet by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var selectedHistoryTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(currentUser.uid) {
        viewModel.startWithdrawalsListener(currentUser.uid)
    }

    val filteredWithdrawals = remember(withdrawals, selectedHistoryTab) {
        when (selectedHistoryTab) {
            1 -> withdrawals.filter { it.status.lowercase() in listOf("pending", "processing", "process") }
            2 -> withdrawals.filter { it.status.lowercase() in listOf("completed", "complete") }
            else -> withdrawals
        }
    }

    LaunchedEffect(walletState) {
        if (walletState is WalletState.Success) {
            showSuccessDialog = true
            showWithdrawSheet = false
            amountText = ""
        }
    }

    val pointsInRupees = currentUser.points.toDouble() / 1000.0

    // Shine animation on balance card
    val inf = rememberInfiniteTransition(label = "wallet_inf")
    val shimmerX by inf.animateFloat(
        -300f, 800f,
        infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer"
    )

    Box(modifier = Modifier.fillMaxSize().background(GradientBg)) {
        // Background orbs
        Box(
            modifier = Modifier.size(240.dp).align(Alignment.TopStart)
                .offset(x = (-60).dp, y = (-40).dp).blur(70.dp)
                .background(Indigo.copy(.3f), CircleShape)
        )
        Box(
            modifier = Modifier.size(200.dp).align(Alignment.BottomEnd)
                .offset(x = 50.dp, y = 50.dp).blur(60.dp)
                .background(Rose.copy(.2f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // ── Page Header ──
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("My Wallet", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    Text("Earnings & withdrawals", fontSize = 12.sp, color = TextSecondary)
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Indigo.copy(.15f))
                        .border(1.dp, Indigo.copy(.3f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, null, tint = Indigo, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Premium Balance Card ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(28.dp))
            ) {
                // Card base gradient
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF4C1D95)))
                        )
                )
                // Moving shimmer overlay
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(.06f),
                                    Color.Transparent
                                ),
                                startX = shimmerX,
                                endX   = shimmerX + 300f
                            )
                        )
                )
                // Decorative circles inside card
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 40.dp, y = (-40).dp)
                        .blur(30.dp)
                        .background(Violet.copy(.3f), CircleShape)
                )

                // Card content
                Column(modifier = Modifier.padding(28.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("TOTAL BALANCE", fontSize = 11.sp, color = Color.White.copy(.6f), letterSpacing = 2.sp)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(.12f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("ACTIVE", fontSize = 10.sp, color = Emerald, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "${currentUser.points}",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text("Points", fontSize = 14.sp, color = Color.White.copy(.55f))

                    Spacer(Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(.15f))
                    )

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("EQUIVALENT VALUE", fontSize = 10.sp, color = Color.White.copy(.5f), letterSpacing = 1.5.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "₹%.2f".format(pointsInRupees),
                                fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = GoldLight
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("RATE", fontSize = 10.sp, color = Color.White.copy(.5f), letterSpacing = 1.5.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("1000 pts = ₹1", fontSize = 12.sp, color = Color.White.copy(.7f), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Stats row ──
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WalletStatCard(
                    modifier = Modifier.weight(1f),
                    label    = "Today Earned",
                    value    = "+${currentUser.todayEarning} pts",
                    tint     = Emerald,
                    icon     = Icons.Default.TrendingUp
                )
                WalletStatCard(
                    modifier = Modifier.weight(1f),
                    label    = "Total Withdrawn",
                    value    = "₹%.2f".format(currentUser.totalEarnings),
                    tint     = Gold,
                    icon     = Icons.Default.AccountBalanceWallet
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Withdraw Button ──
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                // Glow
                Box(
                    modifier = Modifier
                        .fillMaxWidth().height(58.dp).blur(16.dp)
                        .background(GradientGold, RoundedCornerShape(20.dp))
                )
                Button(
                    onClick  = { showWithdrawSheet = true },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape    = RoundedCornerShape(20.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(GradientGold, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💸", fontSize = 18.sp)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Withdraw Money",
                                fontSize = 17.sp, fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1C1504)
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, null, tint = Color(0xFF1C1504), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Minimum withdrawal: ₹10 (10,000 points)",
                fontSize = 11.sp, color = TextSecondary, textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // ── Info card ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgCard)
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(32.dp).background(Indigo.copy(.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) { Text("ℹ️", fontSize = 14.sp) }
                        Spacer(Modifier.width(10.dp))
                        Text("How it Works", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(Modifier.height(14.dp))
                    listOf(
                        "Enter UPI ID and withdrawal amount",
                        "Admin reviews and approves request",
                        "Money sent within 24–48 hours",
                        "Minimum withdrawal: ₹10"
                    ).forEachIndexed { i, step ->
                        Row(modifier = Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(Indigo.copy(.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${i + 1}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = IndigoLight)
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(step, fontSize = 13.sp, color = TextSecondary, lineHeight = 20.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Withdrawal History Header ──
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Withdrawal History",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${filteredWithdrawals.size} records",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Spacer(Modifier.height(12.dp))

            // Tab Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Pending", "Completed").forEachIndexed { index, title ->
                    val isSelected = selectedHistoryTab == index
                    val chipBg = if (isSelected) Indigo.copy(.2f) else BgCard
                    val chipBorder = if (isSelected) Indigo else GlassBorder
                    val chipText = if (isSelected) IndigoLight else TextSecondary

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(chipBg)
                            .border(1.dp, chipBorder, RoundedCornerShape(12.dp))
                            .clickable { selectedHistoryTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = chipText
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── History List / Empty State ──
            if (filteredWithdrawals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(BgCard)
                        .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📭", fontSize = 32.sp)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "No withdrawal requests found",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    filteredWithdrawals.forEach { request ->
                        WithdrawalRequestCard(request = request)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            BannerAdView()
        }
    }

    // ── Withdraw Dialog ──
    if (showWithdrawSheet) {
        AlertDialog(
            onDismissRequest = { showWithdrawSheet = false; viewModel.resetState() },
            containerColor   = BgCard,
            shape            = RoundedCornerShape(28.dp),
            title = {
                Column {
                    Text("💸 Withdraw Funds", fontWeight = FontWeight.ExtraBold, color = TextPrimary, fontSize = 20.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Balance: ${currentUser.points} pts ≈ ₹%.2f".format(pointsInRupees),
                        fontSize = 12.sp, color = TextSecondary
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    PremiumTextField(
                        value         = upiId,
                        onValueChange = { upiId = it },
                        label         = "UPI ID",
                        placeholder   = "e.g. name@paytm"
                    )
                    PremiumTextField(
                        value         = amountText,
                        onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                        label         = "Amount (₹)",
                        placeholder   = "Minimum ₹10",
                        keyboardType  = KeyboardType.Number
                    )
                    if (walletState is WalletState.Error) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                .background(Rose.copy(.1f)).border(1.dp, Rose.copy(.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text((walletState as WalletState.Error).message, color = Rose, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitWithdrawal(currentUser, upiId.trim(), amountText.toIntOrNull() ?: 0, onUserUpdate)
                    },
                    enabled  = walletState !is WalletState.Loading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(GradientPrimary, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (walletState is WalletState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Submit Request", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawSheet = false; viewModel.resetState() }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // ── Success Dialog ──
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false; viewModel.resetState() },
            containerColor   = BgCard,
            shape            = RoundedCornerShape(28.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier.size(72.dp).background(Emerald.copy(.15f), CircleShape)
                            .border(2.dp, Emerald.copy(.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Emerald, modifier = Modifier.size(38.dp))
                    }
                    Spacer(Modifier.height(14.dp))
                    Text("Request Submitted!", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = TextPrimary)
                }
            },
            text = {
                Text(
                    "Your withdrawal request is pending admin approval. You'll receive money in 24–48 hours.",
                    fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick  = { showSuccessDialog = false; viewModel.resetState() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Emerald)
                ) { Text("Awesome! 🎉", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        )
    }
}

@Composable
private fun WalletStatCard(modifier: Modifier, label: String, value: String, tint: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(tint.copy(.09f))
            .border(1.dp, tint.copy(.2f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column {
            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = tint)
            Text(label, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label, color = TextSecondary, fontSize = 13.sp) },
        placeholder   = { Text(placeholder, color = TextMuted, fontSize = 13.sp) },
        modifier      = Modifier.fillMaxWidth(),
        singleLine    = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape         = RoundedCornerShape(14.dp),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Indigo,
            unfocusedBorderColor = GlassBorder,
            focusedTextColor     = TextPrimary,
            unfocusedTextColor   = TextPrimary,
            cursorColor          = Indigo,
            focusedLabelColor    = Indigo
        )
    )
}

@Composable
private fun WithdrawalRequestCard(request: WithdrawalRequest) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BgCard)
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "UPI: ${request.upiId}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                val dateStr = remember(request.timestamp) {
                    val date = request.timestamp?.toDate()
                    if (date != null) {
                        java.text.SimpleDateFormat("MMM dd, yyyy • hh:mm a", java.util.Locale.getDefault()).format(date)
                    } else {
                        "Just now"
                    }
                }
                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${request.amount}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = when (request.status.lowercase()) {
                        "completed", "complete" -> Emerald
                        "rejected", "reject", "failed", "fail" -> Rose
                        else -> Gold
                    }
                )
                Spacer(Modifier.height(6.dp))
                StatusBadge(status = request.status)
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status.lowercase()) {
        "completed", "complete" -> Triple(Emerald.copy(.15f), Emerald, "Completed")
        "rejected", "reject", "failed", "fail" -> Triple(Rose.copy(.15f), Rose, "Rejected")
        else -> Triple(Gold.copy(.15f), Gold, "Pending")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            letterSpacing = 1.sp
        )
    }
}
