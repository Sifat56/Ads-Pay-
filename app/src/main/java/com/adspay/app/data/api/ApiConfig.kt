package com.adspay.app.data.api

object ApiConfig {
    /**
     * Centralized Base URL for Ads Pay Cloud Server / Backend API.
     * 
     * To point to a custom domain or VPS, simply update this URL.
     * Example: "https://your-production-server.com" or "https://api.adspay.app"
     */
    const val DEFAULT_BASE_URL: String = "https://ais-dev-e5y5vcfysqgthqigmaqghs-275933888173.asia-southeast1.run.app"

    var currentBaseUrl: String = DEFAULT_BASE_URL

    fun getBaseUrl(): String {
        return currentBaseUrl.trimEnd('/')
    }

    fun buildUrl(endpoint: String): String {
        val base = getBaseUrl()
        val path = if (endpoint.startsWith("/")) endpoint else "/$endpoint"
        return "$base$path"
    }

    fun updateBaseUrl(newUrl: String) {
        if (newUrl.isNotBlank() && (newUrl.startsWith("http://") || newUrl.startsWith("https://"))) {
            currentBaseUrl = newUrl.trimEnd('/')
        }
    }
}
