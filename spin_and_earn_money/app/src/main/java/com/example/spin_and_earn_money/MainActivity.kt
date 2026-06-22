package com.example.spin_and_earn_money

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.spin_and_earn_money.ads.AdManager
import com.example.spin_and_earn_money.ads.AppOpenAdManager
import com.example.spin_and_earn_money.data.UserModel
import com.example.spin_and_earn_money.ui.screens.HomeScreen
import com.example.spin_and_earn_money.ui.screens.LoginScreen
import com.example.spin_and_earn_money.ui.theme.Spin_and_earn_moneyTheme
import com.example.spin_and_earn_money.viewmodel.AuthState
import com.example.spin_and_earn_money.viewmodel.AuthViewModel
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp

private const val ROUTE_LOGIN = "login"
private const val ROUTE_HOME  = "home"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize Mobile Ads SDK and load ads after initialization completes
        MobileAds.initialize(this) {
            AppOpenAdManager.setInitialized()
            AdManager.setInitialized()

            // Register App Open lifecycle observer AFTER initialization to avoid race conditions
            application.registerActivityLifecycleCallbacks(AppOpenAdManager)

            AppOpenAdManager.loadAd(this)
            AdManager.loadRewardedAd(this)
            AdManager.loadInterstitialAd(this)
            AdManager.loadRewardedInterstitialAd(this)
        }

        setContent {
            Spin_and_earn_moneyTheme(darkTheme = true) {
                AppNavigation()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        application.unregisterActivityLifecycleCallbacks(AppOpenAdManager)
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    // Check if user is already logged in on app start
    LaunchedEffect(Unit) {
        if (authViewModel.isLoggedIn()) {
            authViewModel.checkExistingLogin()
        }
    }

    val authState by authViewModel.authState.collectAsState()

    // Navigate to home when auth succeeds
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            navController.navigate(ROUTE_HOME) {
                popUpTo(ROUTE_LOGIN) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (authViewModel.isLoggedIn()) ROUTE_HOME else ROUTE_LOGIN
    ) {
        composable(ROUTE_LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(ROUTE_HOME) {
                        popUpTo(ROUTE_LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(ROUTE_HOME) {
            val user = (authState as? AuthState.Success)?.user
                ?: authViewModel.currentUser.collectAsState().value
                ?: UserModel()

            HomeScreen(
                authViewModel = authViewModel,
                initialUser   = user,
                onLogout      = {
                    navController.navigate(ROUTE_LOGIN) {
                        popUpTo(ROUTE_HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}