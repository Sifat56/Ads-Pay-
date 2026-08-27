package com.adspay.app.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.startapp.sdk.adsbase.Ad
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppSDK
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener
import com.startapp.sdk.adsbase.adlisteners.AdEventListener
import com.startapp.sdk.adsbase.adlisteners.VideoListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class RewardedAdStatus {
    IDLE,
    LOADING,
    READY,
    SHOWING,
    COMPLETED,
    FAILED
}

object StartIoAdManager {
    private const val TAG = "StartIoAdManager"
    const val APP_ID = "207226080"

    private var isInitialized = false
    private var rewardedAd: StartAppAd? = null

    private val _rewardedAdStatus = MutableStateFlow(RewardedAdStatus.IDLE)
    val rewardedAdStatus: StateFlow<RewardedAdStatus> = _rewardedAdStatus.asStateFlow()

    private val _adErrorMessage = MutableStateFlow<String?>(null)
    val adErrorMessage: StateFlow<String?> = _adErrorMessage.asStateFlow()

    fun initialize(context: Context, isTestAdsEnabled: Boolean = false) {
        if (isInitialized) return
        try {
            StartAppSDK.init(context, APP_ID, false)
            StartAppSDK.enableReturnAds(false)
            if (isTestAdsEnabled) {
                StartAppSDK.setTestAdsEnabled(true)
            }
            isInitialized = true
            Log.d(TAG, "Start.io SDK initialized successfully with App ID: $APP_ID")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Start.io SDK: ${e.message}", e)
        }
    }

    fun loadRewardedAd(context: Context, onLoaded: (() -> Unit)? = null, onFailed: ((String) -> Unit)? = null) {
        try {
            _rewardedAdStatus.value = RewardedAdStatus.LOADING
            _adErrorMessage.value = null

            val startAppAd = StartAppAd(context)
            rewardedAd = startAppAd

            startAppAd.setVideoListener(object : VideoListener {
                override fun onVideoCompleted() {
                    Log.d(TAG, "Start.io Rewarded Video completed successfully!")
                    _rewardedAdStatus.value = RewardedAdStatus.COMPLETED
                }
            })

            startAppAd.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, object : AdEventListener {
                override fun onReceiveAd(ad: Ad) {
                    Log.d(TAG, "Start.io Rewarded Ad loaded successfully.")
                    _rewardedAdStatus.value = RewardedAdStatus.READY
                    onLoaded?.invoke()
                }

                override fun onFailedToReceiveAd(ad: Ad?) {
                    val msg = ad?.errorMessage ?: "Failed to load rewarded video ad"
                    Log.w(TAG, "Start.io Rewarded Ad failed to load: $msg")
                    _rewardedAdStatus.value = RewardedAdStatus.FAILED
                    _adErrorMessage.value = msg
                    onFailed?.invoke(msg)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Exception during loadRewardedAd: ${e.message}", e)
            _rewardedAdStatus.value = RewardedAdStatus.FAILED
            _adErrorMessage.value = e.message ?: "Failed to load ad"
            onFailed?.invoke(e.message ?: "Unknown error")
        }
    }

    fun showRewardedAd(
        activity: Activity,
        onVideoCompleted: () -> Unit,
        onAdClosedWithoutCompletion: () -> Unit = {},
        onFailedToShow: (String) -> Unit = {}
    ) {
        val currentAd = rewardedAd
        if (currentAd == null || !currentAd.isReady) {
            Log.w(TAG, "Rewarded Ad is not ready to show. Attempting reload...")
            onFailedToShow("Ad not ready yet. Please wait a moment and try again.")
            loadRewardedAd(activity)
            return
        }

        var isCompleted = false

        currentAd.setVideoListener(object : VideoListener {
            override fun onVideoCompleted() {
                Log.d(TAG, "Video completed listener callback triggered.")
                isCompleted = true
                _rewardedAdStatus.value = RewardedAdStatus.COMPLETED
                onVideoCompleted()
            }
        })

        _rewardedAdStatus.value = RewardedAdStatus.SHOWING

        val displayed = currentAd.showAd(object : AdDisplayListener {
            override fun adHidden(ad: Ad?) {
                Log.d(TAG, "Rewarded Ad hidden. Completed=$isCompleted")
                _rewardedAdStatus.value = RewardedAdStatus.IDLE
                rewardedAd = null
                if (!isCompleted) {
                    onAdClosedWithoutCompletion()
                }
            }

            override fun adDisplayed(ad: Ad?) {
                Log.d(TAG, "Rewarded Ad displayed on screen.")
            }

            override fun adClicked(ad: Ad?) {
                Log.d(TAG, "Rewarded Ad clicked (Note: clicks are not rewarded according to policy).")
            }

            override fun adNotDisplayed(ad: Ad?) {
                val msg = ad?.errorMessage ?: "Ad could not be displayed."
                Log.w(TAG, "Rewarded Ad not displayed: $msg")
                _rewardedAdStatus.value = RewardedAdStatus.FAILED
                _adErrorMessage.value = msg
                onFailedToShow(msg)
            }
        })

        if (!displayed) {
            Log.w(TAG, "StartAppAd.showAd returned false")
            _rewardedAdStatus.value = RewardedAdStatus.FAILED
            onFailedToShow("Ad could not be presented.")
        }
    }

    fun resetStatus() {
        _rewardedAdStatus.value = RewardedAdStatus.IDLE
        _adErrorMessage.value = null
    }
}
