package com.adspay.app.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
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
import java.util.concurrent.atomic.AtomicBoolean

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
    private var isAdLoading = false
    private var retryCount = 0
    private const val MAX_RETRIES = 3
    private val mainHandler = Handler(Looper.getMainLooper())

    private val pendingLoadedCallbacks = mutableListOf<() -> Unit>()
    private val pendingFailedCallbacks = mutableListOf<(String) -> Unit>()

    private val _rewardedAdStatus = MutableStateFlow(RewardedAdStatus.IDLE)
    val rewardedAdStatus: StateFlow<RewardedAdStatus> = _rewardedAdStatus.asStateFlow()

    private val _adErrorMessage = MutableStateFlow<String?>(null)
    val adErrorMessage: StateFlow<String?> = _adErrorMessage.asStateFlow()

    fun initialize(context: Context, isTestAdsEnabled: Boolean = false) {
        if (isInitialized) return
        try {
            StartAppSDK.init(context.applicationContext, APP_ID, false)
            StartAppSDK.enableReturnAds(false)
            StartAppAd.disableSplash()
            if (isTestAdsEnabled) {
                StartAppSDK.setTestAdsEnabled(true)
            }
            isInitialized = true
            Log.d(TAG, "Start.io SDK initialized successfully with App ID: $APP_ID")
            // Proactively warm-up preload the rewarded ad
            loadRewardedAd(context.applicationContext)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Start.io SDK: ${e.message}", e)
        }
    }

    fun isAdReady(): Boolean {
        return rewardedAd?.isReady == true
    }

    @Synchronized
    fun loadRewardedAd(
        context: Context,
        onLoaded: (() -> Unit)? = null,
        onFailed: ((String) -> Unit)? = null
    ) {
        if (!isInitialized) {
            initialize(context)
        }

        if (onLoaded != null) {
            pendingLoadedCallbacks.add(onLoaded)
        }
        if (onFailed != null) {
            pendingFailedCallbacks.add(onFailed)
        }

        if (rewardedAd?.isReady == true) {
            _rewardedAdStatus.value = RewardedAdStatus.READY
            _adErrorMessage.value = null
            dispatchLoadedCallbacks()
            return
        }

        if (isAdLoading) {
            // Already loading in progress, callbacks have been queued
            return
        }

        try {
            isAdLoading = true
            _rewardedAdStatus.value = RewardedAdStatus.LOADING
            _adErrorMessage.value = null

            val appContext = context.applicationContext
            val startAppAd = StartAppAd(context)
            rewardedAd = startAppAd

            startAppAd.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, object : AdEventListener {
                override fun onReceiveAd(ad: Ad) {
                    isAdLoading = false
                    retryCount = 0
                    Log.d(TAG, "Start.io Rewarded Video Ad cached and ready to display.")
                    _rewardedAdStatus.value = RewardedAdStatus.READY
                    _adErrorMessage.value = null
                    dispatchLoadedCallbacks()
                }

                override fun onFailedToReceiveAd(ad: Ad?) {
                    val rawError = ad?.errorMessage ?: "No ad fill currently available."
                    Log.w(TAG, "Start.io Rewarded Video load returned: $rawError. Trying automatic fullscreen fallback...")
                    
                    // Fallback to AUTOMATIC mode (Fullpage/Interstitial/Video) if pure REWARDED_VIDEO has no fill (204)
                    val fallbackAd = StartAppAd(context)
                    fallbackAd.loadAd(StartAppAd.AdMode.AUTOMATIC, object : AdEventListener {
                        override fun onReceiveAd(fallbackLoadedAd: Ad) {
                            isAdLoading = false
                            retryCount = 0
                            rewardedAd = fallbackAd
                            Log.d(TAG, "Start.io Fullscreen Fallback Ad cached and ready to display.")
                            _rewardedAdStatus.value = RewardedAdStatus.READY
                            _adErrorMessage.value = null
                            dispatchLoadedCallbacks()
                        }

                        override fun onFailedToReceiveAd(fallbackAdFailed: Ad?) {
                            isAdLoading = false
                            val fallbackErr = fallbackAdFailed?.errorMessage ?: rawError
                            val friendlyError = if (fallbackErr.contains("204") || rawError.contains("204")) {
                                "Ad inventory is currently filling from Start.io. Please retry in a few seconds."
                            } else {
                                fallbackErr
                            }
                            Log.w(TAG, "Start.io Fallback Ad load failed: $fallbackErr")
                            _rewardedAdStatus.value = RewardedAdStatus.FAILED
                            _adErrorMessage.value = friendlyError
                            dispatchFailedCallbacks(friendlyError)

                            // Auto retry with exponential backoff if not exceeded max retries
                            if (retryCount < MAX_RETRIES) {
                                retryCount++
                                val delayMs = (retryCount * 3000L).coerceAtLeast(3000L)
                                mainHandler.postDelayed({
                                    loadRewardedAd(appContext)
                                }, delayMs)
                            }
                        }
                    })
                }
            })
        } catch (e: Exception) {
            isAdLoading = false
            Log.e(TAG, "Exception during loadRewardedAd: ${e.message}", e)
            _rewardedAdStatus.value = RewardedAdStatus.FAILED
            _adErrorMessage.value = e.message ?: "Failed to load ad"
            dispatchFailedCallbacks(e.message ?: "Failed to load ad")
        }
    }

    @Synchronized
    private fun dispatchLoadedCallbacks() {
        val callbacks = ArrayList(pendingLoadedCallbacks)
        pendingLoadedCallbacks.clear()
        pendingFailedCallbacks.clear()
        mainHandler.post {
            callbacks.forEach { it.invoke() }
        }
    }

    @Synchronized
    private fun dispatchFailedCallbacks(errorMsg: String) {
        val callbacks = ArrayList(pendingFailedCallbacks)
        pendingLoadedCallbacks.clear()
        pendingFailedCallbacks.clear()
        mainHandler.post {
            callbacks.forEach { it.invoke(errorMsg) }
        }
    }

    fun showRewardedAd(
        activity: Activity,
        onVideoCompleted: () -> Unit,
        onAdClosedWithoutCompletion: () -> Unit = {},
        onFailedToShow: (String) -> Unit = {}
    ) {
        val currentAd = rewardedAd

        // If ad is not ready yet, perform an immediate fast-load and then show
        if (currentAd == null || !currentAd.isReady) {
            Log.d(TAG, "Rewarded Ad not cached yet. Initiating priority load...")
            _rewardedAdStatus.value = RewardedAdStatus.LOADING

            loadRewardedAd(
                context = activity,
                onLoaded = {
                    mainHandler.post {
                        showRewardedAd(
                            activity = activity,
                            onVideoCompleted = onVideoCompleted,
                            onAdClosedWithoutCompletion = onAdClosedWithoutCompletion,
                            onFailedToShow = onFailedToShow
                        )
                    }
                },
                onFailed = { msg ->
                    mainHandler.post {
                        _rewardedAdStatus.value = RewardedAdStatus.FAILED
                        onFailedToShow("Ad is loading ($msg). Please tap 'Watch Ad' again in a few seconds.")
                    }
                }
            )
            return
        }

        val hasRewarded = AtomicBoolean(false)
        val hasCompletedVideo = AtomicBoolean(false)

        currentAd.setVideoListener(object : VideoListener {
            override fun onVideoCompleted() {
                Log.d(TAG, "Start.io onVideoCompleted event fired!")
                hasCompletedVideo.set(true)
                if (hasRewarded.compareAndSet(false, true)) {
                    mainHandler.post {
                        _rewardedAdStatus.value = RewardedAdStatus.COMPLETED
                        onVideoCompleted()
                    }
                }
            }
        })

        _rewardedAdStatus.value = RewardedAdStatus.SHOWING

        val displayed = currentAd.showAd(object : AdDisplayListener {
            override fun adHidden(ad: Ad?) {
                Log.d(TAG, "Rewarded Ad closed. CompletedVideo=${hasCompletedVideo.get()}, Rewarded=${hasRewarded.get()}")
                _rewardedAdStatus.value = RewardedAdStatus.IDLE
                rewardedAd = null
                
                // Immediately warm-up next ad for next cycle
                loadRewardedAd(activity.applicationContext)

                // If video callback fired or fullscreen ad was successfully viewed to completion
                if (hasCompletedVideo.get() || hasRewarded.get()) {
                    // Already awarded
                } else {
                    // If it was a fullscreen fallback ad successfully displayed and closed, grant reward
                    if (hasRewarded.compareAndSet(false, true)) {
                        mainHandler.post {
                            _rewardedAdStatus.value = RewardedAdStatus.COMPLETED
                            onVideoCompleted()
                        }
                    } else {
                        mainHandler.post {
                            onAdClosedWithoutCompletion()
                        }
                    }
                }
            }

            override fun adDisplayed(ad: Ad?) {
                Log.d(TAG, "Rewarded Ad successfully displayed on screen.")
            }

            override fun adClicked(ad: Ad?) {
                Log.d(TAG, "Rewarded Ad clicked by user.")
            }

            override fun adNotDisplayed(ad: Ad?) {
                val msg = ad?.errorMessage ?: "Ad could not be presented on current screen."
                Log.w(TAG, "Rewarded Ad not displayed: $msg")
                _rewardedAdStatus.value = RewardedAdStatus.FAILED
                _adErrorMessage.value = msg
                rewardedAd = null
                loadRewardedAd(activity.applicationContext)
                mainHandler.post {
                    onFailedToShow(msg)
                }
            }
        })

        if (!displayed) {
            Log.w(TAG, "StartAppAd.showAd returned false. Loading fresh ad...")
            _rewardedAdStatus.value = RewardedAdStatus.FAILED
            rewardedAd = null
            loadRewardedAd(activity.applicationContext)
            mainHandler.post {
                onFailedToShow("Ad could not be presented. Please try again.")
            }
        }
    }

    fun resetStatus() {
        _rewardedAdStatus.value = RewardedAdStatus.IDLE
        _adErrorMessage.value = null
    }
}

