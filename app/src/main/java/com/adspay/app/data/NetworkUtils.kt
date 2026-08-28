package com.adspay.app.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NetworkUtils {
    const val ERROR_NO_INTERNET = "Internet connection is required. Please connect to the internet and try again."

    private val _isOnlineState = MutableStateFlow(false)
    val isOnlineState: StateFlow<Boolean> = _isOnlineState.asStateFlow()

    private var isRegistered = false

    /**
     * Initializes real-time network listener.
     */
    fun init(context: Context) {
        val appContext = context.applicationContext
        _isOnlineState.value = isInternetAvailable(appContext)

        if (isRegistered) return
        try {
            val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return

            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isOnlineState.value = isInternetAvailable(appContext)
                }

                override fun onLost(network: Network) {
                    _isOnlineState.value = isInternetAvailable(appContext)
                }

                override fun onUnavailable() {
                    _isOnlineState.value = false
                }

                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                    val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                            (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ||
                                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                    _isOnlineState.value = hasInternet
                }
            })
            isRegistered = true
        } catch (e: Exception) {
            // Fallback to active query
            _isOnlineState.value = isInternetAvailable(appContext)
        }
    }

    /**
     * Manually triggers a network re-check.
     */
    fun refreshConnectivity(context: Context?): Boolean {
        val status = isInternetAvailable(context)
        _isOnlineState.value = status
        return status
    }

    /**
     * Checks if the device has an active and capable internet connection.
     */
    fun isInternetAvailable(context: Context?): Boolean {
        if (context == null) return false
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false

            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        } catch (e: Exception) {
            false
        }
    }
}

