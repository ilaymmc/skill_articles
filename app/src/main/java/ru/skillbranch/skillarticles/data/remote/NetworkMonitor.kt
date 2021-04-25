package ru.skillbranch.skillarticles.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.MutableLiveData

object NetworkMonitor {
    var isConnected: Boolean = false
    val isConnectedLive = MutableLiveData(false)
    val networkTypeLive = MutableLiveData(NetworkType.NONE)

    private lateinit var cm: ConnectivityManager

    fun registerNetworkMonitor(ctx: Context) {
        cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        obtainNetworkType(cm.activeNetwork?.let {
            cm.getNetworkCapabilities(it)
        }).also {
            networkTypeLive.postValue(it)
            isConnected = (it == NetworkType.WIFI || it == NetworkType.CELLULAR)
        }

        cm.registerNetworkCallback(
            NetworkRequest.Builder().build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    networkTypeLive.postValue(obtainNetworkType(networkCapabilities))
                }

                override fun onLost(network: Network) {
                    setNetworkIsConnected(false)
                }

                override fun onAvailable(network: Network) {
                    setNetworkIsConnected(true)
                }
            }

        )
    }

    private fun obtainNetworkType(networkCapabilities: NetworkCapabilities?) : NetworkType = when {
        networkCapabilities == null -> NetworkType.NONE
        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
        else -> NetworkType.UNKNOWN
    }

    fun setNetworkIsConnected(connected: Boolean) {
        isConnected = connected
        isConnectedLive.postValue(isConnected)
        if (!isConnected)
            networkTypeLive.postValue(NetworkType.NONE)
    }

}

enum class NetworkType {
    NONE, UNKNOWN, WIFI, CELLULAR
}