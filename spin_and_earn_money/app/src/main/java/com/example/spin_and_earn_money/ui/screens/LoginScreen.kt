package com.example.spin_and_earn_money.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spin_and_earn_money.viewmodel.AuthState
import com.example.spin_and_earn_money.viewmodel.AuthViewModel

// ─────────────────────────────────────────────
// Premium Design Token System
// ─────────────────────────────────────────────
val BgDeep        = Color(0xFF060818)          // Deepest background
val BgNavy        = Color(0xFF0B0F24)          // Main background
val BgCard        = Color(0xFF111530)          // Card surface
val BgCardAlt     = Color(0xFF161B38)          // Alternative card

val Indigo        = Color(0xFF6366F1)          // Primary indigo
val IndigoLight   = Color(0xFF818CF8)          // Light indigo
val IndigoDark    = Color(0xFF4338CA)          // Dark indigo
val Violet        = Color(0xFF8B5CF6)          // Secondary violet
val Gold          = Color(0xFFF59E0B)          // Amber gold
val GoldLight     = Color(0xFFFCD34D)          // Light gold
val Emerald       = Color(0xFF10B981)          // Success green
val Rose          = Color(0xFFEC4899)          // Accent rose
val Sky           = Color(0xFF38BDF8)          // Info blue

val TextPrimary   = Color(0xFFF8FAFC)          // Bright white text
val TextSecondary = Color(0xFF94A3B8)          // Slate text
val TextMuted     = Color(0xFF475569)          // Muted text
val GlassWhite    = Color(0x1AFFFFFF)          // 10% white glass
val GlassBorder   = Color(0x26FFFFFF)          // 15% white border

// Legacy aliases so other screens compile without changes
val DarkBg        = BgNavy
val DeepPurple    = Indigo
val VibrantGold   = Gold
val SoftPurple    = Violet
val AccentPink    = Rose
val CardBg        = BgCard
val TextWhite     = TextPrimary
val TextGray      = TextSecondary
val GreenAccent   = Emerald

// Gradient helpers
val GradientPrimary = Brush.linearGradient(listOf(Indigo, Violet, Rose))
val GradientGold    = Brush.linearGradient(listOf(Gold, GoldLight, Gold))
val GradientBg      = Brush.verticalGradient(listOf(BgDeep, BgNavy, Color(0xFF0D1229)))
val GradientCard    = Brush.linearGradient(listOf(Indigo.copy(.35f), Violet.copy(.2f)))

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity
    val authState by viewModel.authState.collectAsState()

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result -> viewModel.handleSignInResult(result.data) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) onLoginSuccess()
    }

    // Floating orb animation
    val inf = rememberInfiniteTransition(label = "bg")
    val orbY by inf.animateFloat(
        initialValue = 0f, targetValue = 30f,
        animationSpec = infiniteRepeatable(tween(3000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "orbY"
    )
    val logoScale by inf.animateFloat(
        initialValue = 0.96f, targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "logoScale"
    )
    val glowAlpha by inf.animateFloat(
        initialValue = 0.4f, targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(1800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GradientBg)
    ) {
        // Background glow orbs
        Box(
            modifier = Modifier
                .size(380.dp)
                .offset(x = (-100).dp, y = (-60 + orbY).dp)
                .blur(80.dp)
                .background(
                    Brush.radialGradient(listOf(Indigo.copy(0.5f), Color.Transparent)),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = (60 - orbY).dp)
                .blur(60.dp)
                .background(
                    Brush.radialGradient(listOf(Rose.copy(0.35f), Color.Transparent)),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-30).dp, y = (-orbY).dp)
                .blur(50.dp)
                .background(
                    Brush.radialGradient(listOf(Violet.copy(0.3f), Color.Transparent)),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Logo Badge ──
            Box(contentAlignment = Alignment.Center) {
                // Outer glow ring
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .blur(20.dp)
                        .background(
                            Brush.radialGradient(listOf(Indigo.copy(glowAlpha), Color.Transparent)),
                            CircleShape
                        )
                )
                // Glass ring
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(logoScale)
                        .border(2.dp, GlassBorder, CircleShape)
                        .background(GlassWhite, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(
                                Brush.radialGradient(listOf(Indigo, IndigoDark, BgCard)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎰", fontSize = 52.sp)
                    }
                }
            }

            Spacer(Modifier.height(36.dp))

            // ── Headline ──
            Text(
                text = "Spin & Earn",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                letterSpacing = (-1).sp
            )
            Box(
                modifier = Modifier
                    .background(GradientPrimary, RoundedCornerShape(6.dp))
                    .padding(horizontal = 16.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "MONEY",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Spin the wheel · Earn real rewards\nWithdraw instantly to your UPI",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(40.dp))

            // ── Feature pills row ──
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeaturePill("🎡", "Spin Daily", Indigo)
                FeaturePill("💎", "Earn Points", Violet)
                FeaturePill("⚡", "UPI Payout", Gold)
            }

            Spacer(Modifier.height(48.dp))

            val isLoading = authState is AuthState.Loading

            // ── Google Sign-In Button ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.radialGradient(listOf(Indigo.copy(.2f), Color.Transparent)))
                    .border(
                        1.5.dp,
                        Brush.linearGradient(listOf(GlassBorder, GlassWhite, GlassBorder)),
                        RoundedCornerShape(18.dp)
                    )
            ) {
                Button(
                    onClick = {
                        val intent = viewModel.getGoogleSignInClient(activity).signInIntent
                        signInLauncher.launch(intent)
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        disabledContainerColor = Color.White.copy(.7f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Indigo,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Google G logo using text
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF4285F4), Color(0xFF34A853))
                                        ),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("G", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                            Spacer(Modifier.width(14.dp))
                            Text(
                                text = "Continue with Google",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }
                }
            }

            // Error banner
            if (authState is AuthState.Error) {
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Rose.copy(.12f))
                        .border(1.dp, Rose.copy(.3f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "⚠ " + (authState as AuthState.Error).message,
                        color = Rose,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = "By continuing you agree to our Terms & Privacy Policy",
                fontSize = 11.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FeaturePill(emoji: String, label: String, tint: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(tint.copy(.12f))
            .border(1.dp, tint.copy(.25f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 22.sp)
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, color = tint, fontWeight = FontWeight.SemiBold)
        }
    }
}
