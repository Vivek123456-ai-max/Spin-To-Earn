package com.example.spin_and_earn_money.ui.screens

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.spin_and_earn_money.ads.AdManager
import com.example.spin_and_earn_money.ads.BannerAdView
import com.example.spin_and_earn_money.data.UserModel
import com.example.spin_and_earn_money.viewmodel.SpinState
import com.example.spin_and_earn_money.viewmodel.SpinViewModel
import kotlin.math.cos
import kotlin.math.sin

// Wheel segment colors — rich, jewel-tone palette
private val segColors = listOf(
    Color(0xFF6366F1), // indigo
    Color(0xFFF59E0B), // gold
    Color(0xFF8B5CF6), // violet
    Color(0xFF10B981), // emerald
    Color(0xFFEC4899), // rose
    Color(0xFF38BDF8), // sky
    Color(0xFFF97316), // orange
    Color(0xFFE11D48)  // crimson
)

@Composable
fun SpinScreen(
    viewModel: SpinViewModel,
    currentUser: UserModel,
    onUserUpdate: (UserModel) -> Unit
) {
    val context   = LocalContext.current
    val activity  = context as Activity
    val spinState by viewModel.spinState.collectAsState()
    val targetAngle by viewModel.targetAngle.collectAsState()
    val scope     = rememberCoroutineScope()

    var showResultDialog          by remember { mutableStateOf(false) }
    var resultPoints              by remember { mutableLongStateOf(0L) }
    var showMaxSpinsDialog        by remember { mutableStateOf(false) }
    var bonusSpinGranted          by remember { mutableStateOf(false) }
    var showBonusSpinEarnedDialog by remember { mutableStateOf(false) }
    var rewardedInterstitialReady by remember { mutableStateOf(AdManager.isRewardedInterstitialReady()) }

    val rotation = remember { Animatable(0f) }

    // Continuous glow pulse on button
    val inf = rememberInfiniteTransition(label = "spin_inf")
    val btnGlow by inf.animateFloat(
        0.85f, 1f,
        infiniteRepeatable(tween(1000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "btnGlow"
    )
    val rimRotation by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label = "rimRot"
    )

    LaunchedEffect(spinState) {
        when (spinState) {
            is SpinState.ShowAd -> {
                rotation.animateTo(
                    targetValue   = targetAngle,
                    animationSpec = tween(3500, easing = FastOutSlowInEasing)
                )
                AdManager.showRewardedAd(
                    activity    = activity,
                    onRewarded  = { viewModel.onAdWatched() },
                    onDismissed = {}
                )
            }
            is SpinState.Done -> {
                resultPoints = (spinState as SpinState.Done).points
                showResultDialog = true
            }
            is SpinState.MaxSpinsReached -> showMaxSpinsDialog = true
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        AdManager.loadRewardedAd(context)
        AdManager.loadRewardedInterstitialAd(context)
    }

    // Poll readiness so the bonus button appears as soon as the ad is cached
    LaunchedEffect(Unit) {
        while (true) {
            rewardedInterstitialReady = AdManager.isRewardedInterstitialReady()
            kotlinx.coroutines.delay(1000)
        }
    }

    val isSpinning = spinState is SpinState.Spinning || spinState is SpinState.ShowAd

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GradientBg)
    ) {
        // Background accent orbs
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-30).dp)
                .blur(70.dp)
                .background(Indigo.copy(.35f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-40).dp, y = 40.dp)
                .blur(60.dp)
                .background(Violet.copy(.3f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            // ── Header ──
            Row(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment   = Alignment.CenterVertically
            ) {
                Column {
                    Text("Spin & Win", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    Text("Up to 10 spins per day", fontSize = 12.sp, color = TextSecondary)
                }
                // Trophy badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Gold.copy(.15f))
                        .border(1.dp, Gold.copy(.3f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EmojiEvents, null, tint = Gold, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("${currentUser.points}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gold)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Stats row ──
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SpinStatCard(
                    modifier = Modifier.weight(1f),
                    label    = "Total Points",
                    value    = "${currentUser.points}",
                    tint     = Indigo,
                    emoji    = "💎"
                )
                SpinStatCard(
                    modifier = Modifier.weight(1f),
                    label    = "Today Earned",
                    value    = "+${currentUser.todayEarning}",
                    tint     = Emerald,
                    emoji    = "✨"
                )
                SpinStatCard(
                    modifier = Modifier.weight(1f),
                    label    = "Spins Left",
                    value    = "${10 - currentUser.spinsToday}",
                    tint     = Gold,
                    emoji    = "🎡"
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── Spin counter stars ──
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(10) { idx ->
                    val used = idx < currentUser.spinsToday
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (used) Gold else TextMuted.copy(.5f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Text(
                "${currentUser.spinsToday}/10 spins used",
                fontSize = 11.sp,
                color    = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(28.dp))

            // ── Spinning Wheel ──
            Box(
                modifier        = Modifier.size(320.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer decorative rotating gradient ring
                Canvas(
                    modifier = Modifier
                        .size(316.dp)
                        .rotate(rimRotation)
                ) {
                    val stroke = Stroke(width = 4f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(20f, 12f)))
                    drawCircle(
                        brush       = Brush.sweepGradient(listOf(Indigo, Violet, Rose, Gold, Indigo)),
                        style       = stroke,
                        radius      = size.minDimension / 2f - 2f
                    )
                }

                // Glow behind wheel
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .blur(24.dp)
                        .background(Indigo.copy(.25f), CircleShape)
                )

                // Wheel
                Canvas(
                    modifier = Modifier
                        .size(280.dp)
                        .rotate(rotation.value)
                        .clip(CircleShape)
                ) {
                    drawWheel(viewModel.rewards, segColors)
                }

                // Center orb
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            Brush.radialGradient(listOf(BgCard, BgDeep)),
                            CircleShape
                        )
                        .border(3.dp, GlassBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Brush.radialGradient(listOf(Indigo, IndigoDark)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎰", fontSize = 22.sp)
                    }
                }

                // Pointer triangle at top
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-2).dp)
                        .zIndex(10f)
                ) {
                    Canvas(modifier = Modifier.size(width = 20.dp, height = 28.dp)) {
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(size.width / 2f, 0f)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(path, color = Gold)
                        // Shadow
                        drawPath(path, color = Color(0xFFF97316).copy(.4f), style = Stroke(width = 2f))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Spin Button ──
            Box(
                modifier        = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(62.dp),
                contentAlignment = Alignment.Center
            ) {
                // Glow
                if (!isSpinning && currentUser.spinsToday < 10) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(62.dp)
                            .scale(btnGlow)
                            .blur(18.dp)
                            .background(
                                Brush.horizontalGradient(listOf(Indigo.copy(.7f), Violet.copy(.7f), Rose.copy(.5f))),
                                RoundedCornerShape(22.dp)
                            )
                    )
                }
                Button(
                    onClick = {
                        if (!isSpinning && currentUser.spinsToday < 10) {
                            scope.launch { rotation.snapTo(rotation.value % 360f) }
                            viewModel.spin(currentUser, onUserUpdate)
                        } else if (currentUser.spinsToday >= 10) {
                            showMaxSpinsDialog = true
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    shape    = RoundedCornerShape(22.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp),
                    elevation      = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (isSpinning || currentUser.spinsToday >= 10)
                                    Brush.horizontalGradient(listOf(TextMuted.copy(.3f), TextMuted.copy(.2f)))
                                else
                                    Brush.horizontalGradient(listOf(Indigo, Violet, Rose)),
                                RoundedCornerShape(22.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSpinning) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Gold, strokeWidth = 2.5.dp)
                                Spacer(Modifier.width(12.dp))
                                Text("Spinning...", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (currentUser.spinsToday >= 10) "No Spins Left Today" else "SPIN NOW",
                                    fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Bonus Spin via Rewarded Interstitial ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                val btnEnabled = !isSpinning && rewardedInterstitialReady && !bonusSpinGranted
                val btnLabel = when {
                    bonusSpinGranted        -> "Bonus Spin Used ✓"
                    !rewardedInterstitialReady -> "Loading Ad..."
                    else                    -> "🎬  Watch Ad for +1 Spin"
                }
                val btnGradient = if (btnEnabled)
                    Brush.horizontalGradient(listOf(Color(0xFF0F766E), Color(0xFF059669), Color(0xFF10B981)))
                else
                    Brush.horizontalGradient(listOf(TextMuted.copy(.25f), TextMuted.copy(.15f)))

                // Glow behind button when ready
                if (btnEnabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .blur(14.dp)
                            .background(
                                Brush.horizontalGradient(listOf(Emerald.copy(.6f), Emerald.copy(.3f))),
                                RoundedCornerShape(18.dp)
                            )
                    )
                }
                Button(
                    onClick = {
                        if (btnEnabled) {
                            AdManager.showRewardedInterstitialAd(
                                activity   = activity,
                                onRewarded = {
                                    bonusSpinGranted = true
                                    showBonusSpinEarnedDialog = true
                                    // Grant +1 spin by decrementing spinsToday
                                    val updated = currentUser.copy(
                                        spinsToday = (currentUser.spinsToday - 1).coerceAtLeast(0)
                                    )
                                    onUserUpdate(updated)
                                },
                                onDismissed = {
                                    rewardedInterstitialReady = AdManager.isRewardedInterstitialReady()
                                }
                            )
                        }
                    },
                    enabled        = btnEnabled,
                    modifier       = Modifier.fillMaxWidth().height(52.dp),
                    shape          = RoundedCornerShape(18.dp),
                    colors         = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    elevation      = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Box(
                        modifier         = Modifier.fillMaxSize().background(btnGradient, RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = btnLabel,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color      = if (btnEnabled) Color.White else TextMuted
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Rewards legend ──
            Text("Possible Rewards", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier              = Modifier.padding(horizontal = 24.dp)
            ) {
                listOf(10L to Indigo, 25L to Violet, 50L to Gold, 100L to Emerald).forEach { (pts, clr) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(clr.copy(.14f))
                            .border(1.dp, clr.copy(.3f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text("$pts pts", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = clr)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            BannerAdView()
        }
    }

    // ── Result Dialog ──
    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = { showResultDialog = false; viewModel.resetState() },
            containerColor   = BgCard,
            shape            = RoundedCornerShape(28.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Gold.copy(.15f), CircleShape)
                            .border(2.dp, Gold.copy(.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏆", fontSize = 32.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("You Won!", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.horizontalGradient(listOf(Emerald.copy(.2f), Indigo.copy(.2f))))
                            .border(1.dp, Emerald.copy(.3f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 24.dp, vertical = 14.dp)
                    ) {
                        Text(
                            "+$resultPoints Points",
                            fontSize = 32.sp, fontWeight = FontWeight.ExtraBold,
                            color    = Emerald
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Credited to your wallet!", fontSize = 13.sp, color = TextSecondary)
                }
            },
            confirmButton = {
                Button(
                    onClick  = { showResultDialog = false; viewModel.resetState() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(GradientPrimary, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) { Text("Claim Reward 🎊", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp) }
                }
            }
        )
    }

    // ── Bonus Spin Earned Dialog ──
    if (showBonusSpinEarnedDialog) {
        AlertDialog(
            onDismissRequest = { showBonusSpinEarnedDialog = false },
            containerColor   = BgCard,
            shape            = RoundedCornerShape(28.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("🎬", fontSize = 40.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("+1 Bonus Spin!", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Emerald)
                }
            },
            text = {
                Text(
                    "You earned 1 extra spin by watching an ad. Enjoy!",
                    color = TextSecondary, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(), lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick  = { showBonusSpinEarnedDialog = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Emerald)
                ) { Text("Let's Spin! 🎰", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        )
    }

    // ── Max spins Dialog ──
    if (showMaxSpinsDialog) {
        AlertDialog(
            onDismissRequest = { showMaxSpinsDialog = false; viewModel.resetState() },
            containerColor   = BgCard,
            shape            = RoundedCornerShape(28.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("😴", fontSize = 40.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Daily Limit Reached", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                }
            },
            text = {
                Text(
                    "You've used all 10 spins for today.\nCome back tomorrow for more rewards!",
                    color = TextSecondary, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(), lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick  = { showMaxSpinsDialog = false; viewModel.resetState() },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Indigo)
                ) { Text("Got it!", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        )
    }
}

@Composable
private fun SpinStatCard(
    modifier: Modifier,
    label: String,
    value: String,
    tint: Color,
    emoji: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(tint.copy(.1f))
            .border(1.dp, tint.copy(.22f), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 18.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = tint)
            Text(label, fontSize = 9.sp, color = TextSecondary, textAlign = TextAlign.Center, lineHeight = 12.sp)
        }
    }
}

private fun DrawScope.drawWheel(rewards: List<Long>, colors: List<Color>) {
    val segCount  = rewards.size
    val sweep     = 360f / segCount
    val radius    = size.minDimension / 2f
    val center    = Offset(size.width / 2f, size.height / 2f)
    val nCanvas   = drawContext.canvas.nativeCanvas

    rewards.forEachIndexed { i, reward ->
        val startAngle = i * sweep - 90f

        // Segment fill
        drawArc(colors[i % colors.size], startAngle, sweep, useCenter = true, size = Size(size.width, size.height))

        // Lighter tinted inner arc (shine effect)
        drawArc(
            color      = Color.White.copy(.08f),
            startAngle = startAngle,
            sweepAngle = sweep * 0.5f,
            useCenter  = true,
            size       = Size(size.width, size.height)
        )

        // Text
        val mid     = Math.toRadians((startAngle + sweep / 2).toDouble())
        val tRadius = radius * 0.62f
        val tx      = center.x + tRadius * cos(mid).toFloat()
        val ty      = center.y + tRadius * sin(mid).toFloat()
        val tRot    = startAngle + sweep / 2 + 90f

        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 34f
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
            setShadowLayer(6f, 0f, 2f, android.graphics.Color.argb(160, 0, 0, 0))
        }
        nCanvas.save()
        nCanvas.rotate(tRot, tx, ty)
        nCanvas.drawText("$reward", tx, ty + 8f, paint)
        nCanvas.restore()

        // Divider line
        val lx = center.x + radius * cos(Math.toRadians(startAngle.toDouble())).toFloat()
        val ly = center.y + radius * sin(Math.toRadians(startAngle.toDouble())).toFloat()
        drawLine(Color.White.copy(.3f), center, Offset(lx, ly), strokeWidth = 1.5f)
    }

    // Metallic outer rim
    drawCircle(
        brush  = Brush.sweepGradient(listOf(Color.White.copy(.6f), Color.White.copy(.2f), Color.White.copy(.6f))),
        style  = Stroke(width = 7f),
        radius = radius
    )
    // Inner shadow ring
    drawCircle(
        color  = Color.Black.copy(.2f),
        style  = Stroke(width = 3f),
        radius = radius - 5f
    )
}
