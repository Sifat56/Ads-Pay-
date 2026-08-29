package com.adspay.app.data.api

import android.content.Context

object ApiConfig {
    /**
     * Centralized Base URL for Ads Pay Cloud Server / Backend API.
     * 
     * To point to a custom domain or VPS, simply update this URL.
     * Example: "https://your-production-server.com" or "https://api.adspay.app"
     */
    const val DEFAULT_BASE_URL: String = "https://ais-pre-e5y5vcfysqgthqigmaqghs-275933888173.asia-southeast1.run.app"
    private const val PREF_KEY_SERVER_URL = "custom_server_base_url"

    var currentBaseUrl: String = DEFAULT_BASE_URL

    fun init(context: Context) {
        try {
            val sp = context.getSharedPreferences("ads_pay_secure_prefs", Context.MODE_PRIVATE)
            val savedUrl = sp.getString(PREF_KEY_SERVER_URL, null)
            if (!savedUrl.isNullOrBlank()) {
                currentBaseUrl = savedUrl.trimEnd('/')
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getBaseUrl(): String {
        return currentBaseUrl.trimEnd('/')
    }

    fun buildUrl(endpoint: String): String {
        val base = getBaseUrl()
        val path = if (endpoint.startsWith("/")) endpoint else "/$endpoint"
        return "$base$path"
    }

    fun updateBaseUrl(newUrl: String, context: Context? = null) {
        if (newUrl.isNotBlank() && (newUrl.startsWith("http://") || newUrl.startsWith("https://"))) {
            currentBaseUrl = newUrl.trimEnd('/')
            if (context != null) {
                try {
                    val sp = context.getSharedPreferences("ads_pay_secure_prefs", Context.MODE_PRIVATE)
                    sp.edit().putString(PREF_KEY_SERVER_URL, currentBaseUrl).apply()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
