package com.example.spin_and_earn_money.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

private const val TAG = "AdManager"

// ── Real Ad Unit IDs ──────────────────────────────────────────────────────────
private const val APP_OPEN_REAL              = "ca-app-pub-7620021466537291/5867664689"
private const val BANNER_REAL                = "ca-app-pub-7620021466537291/2178563047"
private const val INTERSTITIAL_REAL          = "ca-app-pub-7620021466537291/1386705642"
private const val REWARDED_REAL              = "ca-app-pub-7620021466537291/7232717244"
private const val REWARDED_INTERSTITIAL_REAL = "ca-app-pub-7620021466537291/2119991363"

// ── Test Ad Unit IDs ──────────────────────────────────────────────────────────
private const val APP_OPEN_TEST              = "ca-app-pub-3940256099942544/9257395921"
private const val BANNER_TEST                = "ca-app-pub-3940256099942544/6300978111"
private const val INTERSTITIAL_TEST          = "ca-app-pub-3940256099942544/1033173712"
private const val REWARDED_TEST              = "ca-app-pub-3940256099942544/5224354917"
private const val REWARDED_INTERSTITIAL_TEST = "ca-app-pub-3940256099942544/5354046379"

private fun isDebug(context: Context): Boolean {
    return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
}

private fun getAppOpenAdUnitId(context: Context) = if (isDebug(context)) APP_OPEN_TEST else APP_OPEN_REAL
private fun getBannerAdUnitId(context: Context) = if (isDebug(context)) BANNER_TEST else BANNER_REAL
private fun getInterstitialAdUnitId(context: Context) = if (isDebug(context)) INTERSTITIAL_TEST else INTERSTITIAL_REAL
private fun getRewardedAdUnitId(context: Context) = if (isDebug(context)) REWARDED_TEST else REWARDED_REAL
private fun getRewardedInterstitialAdUnitId(context: Context) = if (isDebug(context)) REWARDED_INTERSTITIAL_TEST else REWARDED_INTERSTITIAL_REAL

// ─────────────────────────────────────────────────────────────────────────────
// App Open Ad Manager
// ─────────────────────────────────────────────────────────────────────────────
object AppOpenAdManager : Application.ActivityLifecycleCallbacks {

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd  = false
    private var isInitialized = false

    // FIX: isShowingAd is set to true BEFORE show() is called so that any
    // subsequent onActivityStarted triggered by the ad's own activity won't
    // attempt a double-show.
    var isShowingAd = false
        private set

    fun setInitialized() {
        isInitialized = true
    }

    fun loadAd(context: Context) {
        if (!isInitialized) {
            Log.w(TAG, "AppOpenAdManager not initialized yet")
            return
        }
        if (isLoadingAd || isAdAvailable()) return
        isLoadingAd = true
        AppOpenAd.load(
            context,
            getAppOpenAdUnitId(context),
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd   = ad
                    isLoadingAd = false
                    Log.d(TAG, "App Open ad loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAd   = null
                    isLoadingAd = false
                    Log.e(TAG, "App Open ad failed to load: ${error.message}")
                }
            }
        )
    }

    fun showAdIfAvailable(activity: Activity) {
        // FIX: Also guard against Rewarded / Interstitial ads currently showing
        if (isShowingAd || AdManager.isFullScreenAdShowing) return
        if (!isAdAvailable()) {
            loadAd(activity)
            return
        }

        // FIX: Set flag BEFORE calling show(), not inside onAdShowedFullScreenContent
        isShowingAd = true

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd   = null
                isShowingAd = false
                loadAd(activity)
                Log.d(TAG, "App Open ad dismissed")
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                appOpenAd   = null
                isShowingAd = false
                Log.e(TAG, "App Open ad failed to show: ${error.message}")
            }
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "App Open ad showed")
            }
        }
        appOpenAd?.show(activity)
    }

    private fun isAdAvailable(): Boolean = appOpenAd != null

    // ── ActivityLifecycleCallbacks ────────────────────────────────────────────
    // FIX: Only fire for the app's own MainActivity, not AdMob's ad activities.
    // We check the class name to skip any com.google.android.gms ad activities.
    override fun onActivityStarted(activity: Activity) {
        val isOwnActivity = activity.javaClass.name.startsWith("com.example.spin_and_earn_money")
        if (isOwnActivity && !isShowingAd && !AdManager.isFullScreenAdShowing) {
            showAdIfAvailable(activity)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}

// ─────────────────────────────────────────────────────────────────────────────
// AdManager — Rewarded, Interstitial & Rewarded Interstitial
// ─────────────────────────────────────────────────────────────────────────────
object AdManager {

    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var isInitialized = false

    // FIX: Shared flag so AppOpenAdManager knows a full-screen ad is active
    var isFullScreenAdShowing = false
        private set

    fun setInitialized() {
        isInitialized = true
    }

    // ── Rewarded ──────────────────────────────────────────────────────────────

    fun loadRewardedAd(context: Context) {
        if (!isInitialized) {
            Log.w(TAG, "AdManager not initialized yet")
            return
        }
        RewardedAd.load(context, getRewardedAdUnitId(context), AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    Log.d(TAG, "Rewarded ad loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    Log.e(TAG, "Rewarded ad failed to load: ${error.message}")
                }
            }
        )
    }

    fun showRewardedAd(activity: Activity, onRewarded: () -> Unit, onDismissed: () -> Unit) {
        val ad = rewardedAd
        if (ad != null) {
            isFullScreenAdShowing = true  // FIX: block App Open from firing
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    isFullScreenAdShowing = false
                    rewardedAd = null
                    loadRewardedAd(activity)
                    onDismissed()
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    isFullScreenAdShowing = false
                    rewardedAd = null
                    loadRewardedAd(activity)
                    onDismissed()
                }
            }
            ad.show(activity) { onRewarded() }
        } else {
            Log.w(TAG, "Rewarded ad not ready, simulating reward")
            onRewarded()
            onDismissed()
            loadRewardedAd(activity)
        }
    }

    // ── Interstitial ──────────────────────────────────────────────────────────

    fun loadInterstitialAd(context: Context) {
        if (!isInitialized) {
            Log.w(TAG, "AdManager not initialized yet")
            return
        }
        InterstitialAd.load(context, getInterstitialAdUnitId(context), AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d(TAG, "Interstitial ad loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    Log.e(TAG, "Interstitial ad failed to load: ${error.message}")
                }
            }
        )
    }

    fun showInterstitialAd(activity: Activity, onDismissed: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad != null) {
            isFullScreenAdShowing = true  // FIX: block App Open from firing
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    isFullScreenAdShowing = false
                    interstitialAd = null
                    loadInterstitialAd(activity)
                    onDismissed()
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    isFullScreenAdShowing = false
                    interstitialAd = null
                    loadInterstitialAd(activity)
                    onDismissed()
                }
            }
            ad.show(activity)
        } else {
            onDismissed()
            loadInterstitialAd(activity)
        }
    }

    // ── Rewarded Interstitial ─────────────────────────────────────────────────

    fun loadRewardedInterstitialAd(context: Context) {
        if (!isInitialized) {
            Log.w(TAG, "AdManager not initialized yet")
            return
        }
        RewardedInterstitialAd.load(context, getRewardedInterstitialAdUnitId(context),
            AdRequest.Builder().build(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    rewardedInterstitialAd = ad
                    Log.d(TAG, "Rewarded Interstitial ad loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedInterstitialAd = null
                    Log.e(TAG, "Rewarded Interstitial ad failed to load: ${error.message}")
                }
            }
        )
    }

    fun showRewardedInterstitialAd(
        activity: Activity,
        onRewarded: () -> Unit,
        onDismissed: () -> Unit
    ) {
        val ad = rewardedInterstitialAd
        if (ad != null) {
            isFullScreenAdShowing = true  // FIX: block App Open from firing
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    isFullScreenAdShowing = false
                    rewardedInterstitialAd = null
                    loadRewardedInterstitialAd(activity)
                    onDismissed()
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    isFullScreenAdShowing = false
                    rewardedInterstitialAd = null
                    loadRewardedInterstitialAd(activity)
                    onDismissed()
                }
            }
            ad.show(activity) { onRewarded() }
        } else {
            Log.w(TAG, "Rewarded Interstitial ad not ready")
            onDismissed()
            loadRewardedInterstitialAd(activity)
        }
    }

    fun isRewardedInterstitialReady(): Boolean = rewardedInterstitialAd != null
}

// ─────────────────────────────────────────────────────────────────────────────
// Banner Ad Composable
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun BannerAdView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = getBannerAdUnitId(context)
                    loadAd(AdRequest.Builder().build())
                    adListener = object : AdListener() {
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.e(TAG, "Banner failed: ${error.message}")
                        }
                    }
                }
            }
        )
    }
}
