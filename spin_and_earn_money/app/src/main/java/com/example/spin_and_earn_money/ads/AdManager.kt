package com.example.spin_and_earn_money.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

private const val TAG = "AdManager"

// Ad Unit IDs
private const val BANNER_AD_UNIT_ID = "ca-app-pub-7620021466537291/2178563047"
private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-7620021466537291/1386705642"
private const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

object AdManager {

    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null

    fun loadRewardedAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, REWARDED_AD_UNIT_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                Log.d(TAG, "Rewarded ad loaded")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                rewardedAd = null
                Log.e(TAG, "Rewarded ad failed to load: ${error.message}")
            }
        })
    }

    fun showRewardedAd(
        activity: Activity,
        onRewarded: () -> Unit,
        onDismissed: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAd(activity)
                    onDismissed()
                }
            }
            ad.show(activity) {
                // User earned reward
                onRewarded()
            }
        } else {
            Log.w(TAG, "Rewarded ad not ready, simulating reward")
            // If ad not loaded, still give reward (graceful fallback)
            onRewarded()
            onDismissed()
            loadRewardedAd(activity)
        }
    }

    fun loadInterstitialAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, INTERSTITIAL_AD_UNIT_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                Log.d(TAG, "Interstitial ad loaded")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                interstitialAd = null
                Log.e(TAG, "Interstitial ad failed to load: ${error.message}")
            }
        })
    }

    fun showInterstitialAd(activity: Activity, onDismissed: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
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
}

@Composable
fun BannerAdView() {
    AndroidView(
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = BANNER_AD_UNIT_ID
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
