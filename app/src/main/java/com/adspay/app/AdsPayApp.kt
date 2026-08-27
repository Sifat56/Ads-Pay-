package com.adspay.app

import android.app.Application
import com.adspay.app.ads.StartIoAdManager

class AdsPayApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Start.io Ad SDK with App ID: 207226080
        StartIoAdManager.initialize(this)
    }
}
