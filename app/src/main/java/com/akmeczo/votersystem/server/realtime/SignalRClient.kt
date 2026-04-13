package com.akmeczo.votersystem.server.realtime

import android.util.Log
import com.akmeczo.votersystem.server.Server
import com.akmeczo.votersystem.server.responses.VotingUpdatedDto
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState

class SignalRClient(private val url: String,
                    val server: Server) {
    companion object {
        const val UPDATED_RESULTS_CALLBACK_NAME = "NotifyVotingResultChanged"
    }

    var connection: HubConnection? = null
    val connectionId
        get() = connection?.connectionId
    @Suppress("UNUSED")

    private val connected: Boolean
        get() = connection?.connectionState == HubConnectionState.CONNECTED && connectionId != null

    fun postInit() {
        val authToken = server.authToken
        Log.d("SignalR", "Auth token is $authToken")

        connection = HubConnectionBuilder
            .create(url)
            .withHeader("Cookie", "AuthToken=$authToken")
            .build()
    }

    private val initialised: Boolean
        get() = connection?.connectionState == HubConnectionState.CONNECTED

    fun start(): Boolean {
        try {
            if (!initialised) {
                connection?.start()?.blockingAwait()
            }

            Log.d("SignalR", "Connection started: ${connection?.connectionId}, ${connection?.connectionState}")
            return true
        } catch (e: Exception) {
            Log.w("SignalR", "Error starting connection", e)
        }

        return false
    }

    fun stop() {
        try {
            connection?.stop()
            Log.d("SignalR", "Connection stopped")
        } catch (e: Exception) {
            Log.e("SignalR", "Error stopping connection", e)
        }
    }

    fun registerResultCallback(name: String, callback: (VotingUpdatedDto) -> Unit) {
        connection?.on(name, callback, VotingUpdatedDto::class.java)
    }

    fun deregisterCallback(name: String) {
        connection?.remove(name)
    }
}