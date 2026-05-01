package com.akmeczo.votersystem.server.realtime

import android.util.Log
import com.akmeczo.votersystem.server.Server
import com.akmeczo.votersystem.server.responses.VotingUpdatedDto
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import io.reactivex.rxjava3.core.Completable
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine

class SignalRClient(private val url: String,
                    val server: Server) {
    companion object {
        const val UPDATED_RESULTS_CALLBACK_NAME = "NotifyVotingResultChanged"
        private const val SUBSCRIBE_METHOD_NAME = "SubscribeToVotingAsync"
        private const val UNSUBSCRIBE_METHOD_NAME = "UnsubscribeFromVotingAsync"
    }

    private var connection: HubConnection? = null
    private val subscribedVotingIds = linkedSetOf<Long>()
    private val resultCallbacks = linkedMapOf<String, (VotingUpdatedDto) -> Unit>()

    val connectionId
        get() = connection?.connectionId

    private val connected: Boolean
        get() = connection?.connectionState == HubConnectionState.CONNECTED && connectionId != null

    private fun createConnection(): HubConnection {
        val authToken = server.authToken
        return HubConnectionBuilder
            .create(url)
            .withHeader("Cookie", "AuthToken=$authToken")
            .build()
            .also(::attachCallbacks)
    }

    private fun getOrCreateConnection(): HubConnection {
        val currentConnection = connection
        if (currentConnection != null) {
            return currentConnection
        }

        return createConnection().also { connection = it }
    }

    suspend fun ensureStarted(): Boolean = withContext(Dispatchers.IO) {
        try {
            val currentConnection = getOrCreateConnection()
            if (!connected) {
                currentConnection.start().awaitCompletion()
                resubscribeAll(currentConnection)
            }

            true
        } catch (e: Exception) {
            Log.w("SignalR", "Error starting connection", e)
            false
        }
    }

    suspend fun subscribeToVoting(votingId: Long): Boolean {
        if (!ensureStarted()) {
            return false
        }

        val currentConnection = connection ?: return false
        return try {
            withContext(Dispatchers.IO) {
                currentConnection.invoke(SUBSCRIBE_METHOD_NAME, votingId).awaitCompletion()
            }
            subscribedVotingIds.add(votingId)
            true
        } catch (e: Exception) {
            Log.w("SignalR", "Error subscribing to voting $votingId", e)
            false
        }
    }

    suspend fun unsubscribeFromVoting(votingId: Long): Boolean {
        val currentConnection = connection
        if (currentConnection == null || currentConnection.connectionState != HubConnectionState.CONNECTED) {
            subscribedVotingIds.remove(votingId)
            return true
        }

        return try {
            withContext(Dispatchers.IO) {
                currentConnection.invoke(UNSUBSCRIBE_METHOD_NAME, votingId).awaitCompletion()
            }
            subscribedVotingIds.remove(votingId)
            true
        } catch (e: Exception) {
            Log.w("SignalR", "Error unsubscribing from voting $votingId", e)
            false
        }
    }

    suspend fun disconnect() {
        val currentConnection = connection
        val subscribedIds = subscribedVotingIds.toList()

        for (votingId in subscribedIds) {
            unsubscribeFromVoting(votingId)
        }

        subscribedVotingIds.clear()

        if (currentConnection == null) {
            return
        }

        try {
            withContext(Dispatchers.IO) {
                currentConnection.close()
            }
        } catch (e: Exception) {
            Log.e("SignalR", "Error stopping connection", e)
        } finally {
            connection = null
        }
    }

    private fun attachCallbacks(connection: HubConnection) {
        resultCallbacks.forEach { (name, callback) ->
            connection.on(name, callback, VotingUpdatedDto::class.java)
        }
    }

    private suspend fun resubscribeAll(connection: HubConnection) {
        for (votingId in subscribedVotingIds) {
            connection.invoke(SUBSCRIBE_METHOD_NAME, votingId).awaitCompletion()
        }
    }

    fun registerResultCallback(name: String, callback: (VotingUpdatedDto) -> Unit) {
        resultCallbacks[name] = callback
        connection?.remove(name)
        connection?.on(name, callback, VotingUpdatedDto::class.java)
    }

    fun deregisterCallback(name: String) {
        resultCallbacks.remove(name)
        connection?.remove(name)
    }

    private suspend fun Completable.awaitCompletion() {
        suspendCancellableCoroutine { continuation ->
            val disposable = subscribe(
                { continuation.resume(Unit) },
                { continuation.resumeWithException(it) }
            )
            continuation.invokeOnCancellation { disposable.dispose() }
        }
    }
}
