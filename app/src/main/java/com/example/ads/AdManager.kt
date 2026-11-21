package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdManager {
    private const val TAG = "AdManager"

    // Official Google Test Ad Unit IDs (Safe for testing and development)
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    // Replace with your real AdMob Ad Unit IDs when deploying to Google Play Store:
    var bannerAdUnitId: String = TEST_BANNER_AD_UNIT_ID
    var interstitialAdUnitId: String = TEST_INTERSTITIAL_AD_UNIT_ID
    var rewardedAdUnitId: String = TEST_REWARDED_AD_UNIT_ID

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading: Boolean = false

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading: Boolean = false

    private var lastInterstitialShowTime: Long = 0L
    private const val MIN_INTERSTITIAL_INTERVAL_MS = 45_000L // 45 seconds between interstitials

    private var isInitialized: Boolean = false

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            MobileAds.initialize(context) { initializationStatus ->
                Log.d(TAG, "AdMob MobileAds initialized: $initializationStatus")
                isInitialized = true
                loadInterstitialAd(context)
                loadRewardedAd(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize MobileAds", e)
        }
    }

    fun loadInterstitialAd(context: Context) {
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            interstitialAdUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(TAG, "Interstitial Ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.w(TAG, "Interstitial Ad failed to load: ${error.message}")
                }
            }
        )
    }

    /**
     * Shows an interstitial ad if available and interval has passed.
     * Always calls [onAdDismissed] so user journey continues seamlessly.
     */
    fun showInterstitialAd(
        activity: Activity,
        forceShow: Boolean = false,
        onAdDismissed: () -> Unit
    ) {
        val now = System.currentTimeMillis()
        if (!forceShow && (now - lastInterstitialShowTime < MIN_INTERSTITIAL_INTERVAL_MS)) {
            Log.d(TAG, "Skipping interstitial ad due to frequency capping")
            onAdDismissed()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial Ad dismissed")
                    interstitialAd = null
                    lastInterstitialShowTime = System.currentTimeMillis()
                    loadInterstitialAd(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Interstitial Ad failed to show: ${adError.message}")
                    interstitialAd = null
                    loadInterstitialAd(activity)
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial Ad showed successfully")
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "Interstitial Ad not ready, proceeding")
            loadInterstitialAd(activity)
            onAdDismissed()
        }
    }

    fun isInterstitialReady(): Boolean = interstitialAd != null

    fun loadRewardedAd(context: Context) {
        if (rewardedAd != null || isRewardedLoading) return
        isRewardedLoading = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            rewardedAdUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    Log.d(TAG, "Rewarded Ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    Log.w(TAG, "Rewarded Ad failed to load: ${error.message}")
                }
            }
        )
    }

    /**
     * Shows a rewarded ad. User earns reward upon completion.
     */
    fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: (RewardItem) -> Unit,
        onAdDismissed: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad != null) {
            var rewardGranted = false
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded Ad dismissed, rewardGranted=$rewardGranted")
                    rewardedAd = null
                    loadRewardedAd(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Rewarded Ad failed to show: ${adError.message}")
                    rewardedAd = null
                    loadRewardedAd(activity)
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded Ad showed")
                }
            }

            ad.show(activity) { rewardItem ->
                rewardGranted = true
                Log.d(TAG, "User earned reward: ${rewardItem.type} (${rewardItem.amount})")
                onUserEarnedReward(rewardItem)
            }
        } else {
            Log.d(TAG, "Rewarded Ad not ready, attempting reload")
            loadRewardedAd(activity)
            onAdDismissed()
        }
    }

    fun isRewardedReady(): Boolean = rewardedAd != null
}
