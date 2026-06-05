package com.example.spin_and_earn_money.ui.screens

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.spin_and_earn_money.ads.AdManager
import com.example.spin_and_earn_money.data.UserModel
import com.example.spin_and_earn_money.viewmodel.AuthViewModel
import com.example.spin_and_earn_money.viewmodel.ProfileViewModel
import com.example.spin_and_earn_money.viewmodel.SpinViewModel
import com.example.spin_and_earn_money.viewmodel.WalletViewModel

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Spin    : BottomNavItem("spin",    "Spin",    Icons.Filled.AutoAwesome)
    object Wallet  : BottomNavItem("wallet",  "Wallet",  Icons.Filled.AccountBalanceWallet)
    object Profile : BottomNavItem("profile", "Profile", Icons.Filled.Person)
}

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    initialUser: UserModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity

    val navController    = rememberNavController()
    val spinViewModel:    SpinViewModel    = viewModel()
    val walletViewModel:  WalletViewModel  = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

    val userState by authViewModel.currentUser.collectAsState()
    val currentUser = userState ?: initialUser

    val navItems     = listOf(BottomNavItem.Spin, BottomNavItem.Wallet, BottomNavItem.Profile)
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    var interstitialShownCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { AdManager.loadInterstitialAd(context) }

    Scaffold(
        containerColor = BgNavy,
        bottomBar = {
            // ── Premium Glass Bottom Nav ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        Brush.verticalGradient(listOf(Color.Transparent, BgDeep.copy(.97f)))
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .align(Alignment.BottomCenter)
                        .background(BgDeep.copy(.92f))
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                listOf(Color.Transparent, GlassBorder, GlassBorder, Color.Transparent)
                            ),
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        contentColor = TextPrimary,
                        tonalElevation = 0.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        navItems.forEach { item ->
                            val selected = currentRoute == item.route

                            // Glow animation
                            val inf = rememberInfiniteTransition(label = "nav_${item.route}")
                            val glowScale by inf.animateFloat(
                                initialValue = 1f, targetValue = 1.15f,
                                animationSpec = infiniteRepeatable(
                                    tween(1200, easing = EaseInOutSine), RepeatMode.Reverse
                                ),
                                label = "glow_${item.route}"
                            )

                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (!selected) {
                                        interstitialShownCount++
                                        if (interstitialShownCount % 3 == 0) {
                                            AdManager.showInterstitialAd(activity) {
                                                navController.navigate(item.route) {
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true; restoreState = true
                                                }
                                            }
                                        } else {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                launchSingleTop = true; restoreState = true
                                            }
                                        }
                                    }
                                },
                                icon = {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (selected) {
                                            // Glow behind selected icon
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .scale(glowScale)
                                                    .blur(12.dp)
                                                    .background(Indigo.copy(.5f), CircleShape)
                                            )
                                        }
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label,
                                            modifier = Modifier.size(if (selected) 26.dp else 22.dp),
                                            tint = if (selected) Gold else TextSecondary
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        fontSize = 10.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) Gold else TextSecondary
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor   = Gold,
                                    selectedTextColor   = Gold,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary,
                                    indicatorColor      = Indigo.copy(.2f)
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = BottomNavItem.Spin.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Spin.route) {
                SpinScreen(
                    viewModel    = spinViewModel,
                    currentUser  = currentUser,
                    onUserUpdate = { updated ->
                        authViewModel.updateCurrentUser(updated)
                    }
                )
            }
            composable(BottomNavItem.Wallet.route) {
                WalletScreen(
                    viewModel    = walletViewModel,
                    currentUser  = currentUser,
                    onUserUpdate = { updated ->
                        authViewModel.updateCurrentUser(updated)
                    }
                )
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    viewModel    = profileViewModel,
                    authViewModel= authViewModel,
                    currentUser  = currentUser,
                    onUserUpdate = { updated ->
                        authViewModel.updateCurrentUser(updated)
                    },
                    onLogout = onLogout
                )
            }
        }
    }
}
