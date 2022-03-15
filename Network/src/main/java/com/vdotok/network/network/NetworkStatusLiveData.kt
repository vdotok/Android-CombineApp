package com.vdotok.network.network

import android.content.Context
import android.net.*
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData

class NetworkStatusLiveData(context: Context) : LiveData<Boolean?>() {

    private val connectivityManager: ConnectivityManager
    private val networkCallback: ConnectivityManager.NetworkCallback
    var isDeviceConnected = false

    init {
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                if (!isDeviceConnected) {
                    isDeviceConnected = true
                    Log.d("Network Callback", "Connection is available for network: $network")
                }
//                postValue(true)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                if (isDeviceConnected) {
                    isDeviceConnected = false
                    Log.d("Network Callback", "Connection is lost for network: $network")
                }
//                postValue(false)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) && !isDeviceConnected) {
                        isDeviceConnected = true
                        Log.i(
                            "Network Callback",
                            "Internet Connection is available for network: $network"
                        )
                        postValue(true)
                    } else if (!networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                        && isDeviceConnected) {
                        // handles the scenario when the internet is blocked by ISP,
                        // or when the dsl/fiber/cable line to the router is disconnected
                        isDeviceConnected = false
                        Log.i(
                            "Network Callback",
                            "Internet Connection is lost temporarily for network: $network"
                        )
                        postValue(false)
                    }
                }
            }
        }
    }


    override fun onActive() {
        super.onActive()
//        val info = connectivityManager.activeNetworkInfo
//        if (info != null) {
//            postValue(info.isConnectedOrConnecting)
//        } else {
//            postValue(false)
//        }

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                if (capabilities != null) {
                    when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                            postValue(true)
                        }
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                            postValue(true)
                        }
                    }
                }
            }
            else -> {
                val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
                if (activeNetwork != null) {
                    // connected to the internet
                    when {
                        activeNetwork.type === ConnectivityManager.TYPE_WIFI -> {
                            postValue(true)
                        }
                        activeNetwork.type === ConnectivityManager.TYPE_MOBILE -> {
                            postValue(true)
                        }
                    }
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            val networkRequest = NetworkRequest.Builder().build()
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        }
    }

    override fun onInactive() {
        super.onInactive()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}