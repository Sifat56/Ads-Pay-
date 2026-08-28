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

    private var initialized = false
    private var rewardedAd: StartAppAd? = null

    private val _rewardedAdStatus =
        MutableStateFlow(RewardedAdStatus.IDLE)

    val rewardedAdStatus: StateFlow<RewardedAdStatus> =
        _rewardedAdStatus.asStateFlow()

    private val _adErrorMessage =
        MutableStateFlow<String?>(null)

    val adErrorMessage: StateFlow<String?> =
        _adErrorMessage.asStateFlow()

    fun initialize(
        context: Context,
        isTestAdsEnabled: Boolean = true
    ) {
        if (initialized) return

        try {
            val appContext = context.applicationContext

            StartAppSDK.init(
                appContext,
                APP_ID,
                false
            )

            StartAppSDK.enableReturnAds(false)

            // Development/testing only.
            // Remove or set false before production.
            if (isTestAdsEnabled) {
                StartAppSDK.setTestAdsEnabled(true)
            }

            initialized = true

            Log.d(
                TAG,
                "Start.io initialized. App ID=$APP_ID"
            )

            loadRewardedAd(appContext)

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Start.io initialization failed",
                e
            )

            _rewardedAdStatus.value =
                RewardedAdStatus.FAILED

            _adErrorMessage.value =
                e.message ?: "Start.io initialization failed"
        }
    }

    fun loadRewardedAd(
        context: Context,
        onLoaded: (() -> Unit)? = null,
        onFailed: ((String) -> Unit)? = null
    ) {

        if (!initialized) {
            initialize(context)
            return
        }

        if (_rewardedAdStatus.value ==
            RewardedAdStatus.LOADING
        ) {
            return
        }

        try {

            _rewardedAdStatus.value =
                RewardedAdStatus.LOADING

            _adErrorMessage.value = null

            val ad = StartAppAd(context)

            rewardedAd = ad

            ad.loadAd(
                StartAppAd.AdMode.REWARDED_VIDEO,
                object : AdEventListener {

                    override fun onReceiveAd(ad: Ad) {

                        Log.d(
                            TAG,
                            "Rewarded ad loaded successfully"
                        )

                        _rewardedAdStatus.value =
                            RewardedAdStatus.READY

                        onLoaded?.invoke()
                    }

                    override fun onFailedToReceiveAd(
                        ad: Ad?
                    ) {

                        val error =
                            ad?.errorMessage
                                ?: "Rewarded ad failed to load"

                        Log.e(
                            TAG,
                            "Rewarded ad load failed: $error"
                        )

                        _rewardedAdStatus.value =
                            RewardedAdStatus.FAILED

                        _adErrorMessage.value =
                            error

                        onFailed?.invoke(error)
                    }
                }
            )

        } catch (e: Exception) {

            val error =
                e.message ?: "Exception while loading rewarded ad"

            Log.e(
                TAG,
                error,
                e
            )

            _rewardedAdStatus.value =
                RewardedAdStatus.FAILED

            _adErrorMessage.value =
                error

            onFailed?.invoke(error)
        }
    }

    fun showRewardedAd(
        activity: Activity,
        onVideoCompleted: () -> Unit,
        onAdClosedWithoutCompletion: () -> Unit = {},
        onFailedToShow: (String) -> Unit = {}
    ) {

        val ad = rewardedAd

        if (ad == null || !ad.isReady) {

            Log.w(
                TAG,
                "Rewarded ad is not ready"
            )

            onFailedToShow(
                "Rewarded ad is not ready yet."
            )

            loadRewardedAd(activity)

            return
        }

        var completed = false

        ad.setVideoListener(
            object : VideoListener {

                override fun onVideoCompleted() {

                    Log.d(
                        TAG,
                        "Rewarded video completed"
                    )

                    completed = true

                    _rewardedAdStatus.value =
                        RewardedAdStatus.COMPLETED

                    onVideoCompleted()
                }
            }
        )

        _rewardedAdStatus.value =
            RewardedAdStatus.SHOWING

        try {

            ad.showAd(
                object : AdDisplayListener {

                    override fun adDisplayed(
                        ad: Ad?
                    ) {

                        Log.d(
                            TAG,
                            "Rewarded ad displayed"
                        )
                    }

                    override fun adClicked(
                        ad: Ad?
                    ) {

                        Log.d(
                            TAG,
                            "Rewarded ad clicked"
                        )
                    }

                    override fun adHidden(
                        ad: Ad?
                    ) {

                        Log.d(
                            TAG,
                            "Rewarded ad hidden. Completed=$completed"
                        )

                        rewardedAd = null

                        if (!completed) {
                            onAdClosedWithoutCompletion()
                        }

                        _rewardedAdStatus.value =
                            RewardedAdStatus.IDLE

                        // Preload the next rewarded ad.
                        loadRewardedAd(activity)
                    }

                    override fun adNotDisplayed(
                        ad: Ad?
                    ) {

                        val error =
                            ad?.errorMessage
                                ?: "Rewarded ad could not be displayed"

                        Log.e(
                            TAG,
                            error
                        )

                        rewardedAd = null

                        _rewardedAdStatus.value =
                            RewardedAdStatus.FAILED

                        _adErrorMessage.value =
                            error

                        onFailedToShow(error)

                        // Try to prepare the next ad.
                        loadRewardedAd(activity)
                    }
                }
            )

        } catch (e: Exception) {

            val error =
                e.message ?: "Failed to show rewarded ad"

            Log.e(
                TAG,
                error,
                e
            )

            _rewardedAdStatus.value =
                RewardedAdStatus.FAILED

            _adErrorMessage.value =
                error

            onFailedToShow(error)
        }
    }

    fun resetStatus() {

        _rewardedAdStatus.value =
            RewardedAdStatus.IDLE

        _adErrorMessage.value = null
    }
}
