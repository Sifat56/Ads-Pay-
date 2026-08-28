package com.adspay.app

import android.app.Application
import com.adspay.app.ads.StartIoAdManager
import com.adspay.app.data.NetworkUtils
import com.adspay.app.data.repository.AdsPayRepository

class AdsPayApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Real-time Network Monitor
        NetworkUtils.init(this)
        // Initialize Repository & Storage
        AdsPayRepository.init(this)
        // Initialize Start.io Ad SDK with App ID: 207226080
        StartIoAdManager.initialize(this)
    }
}

