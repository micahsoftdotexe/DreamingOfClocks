package com.micahsoftdotexe.dreamingofclocks.utils

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState

class MediaMetadataHelper(private val context: Context) {

    private var mediaSessionManager: MediaSessionManager? = null
    private var activeController: MediaController? = null
    private var callback: MediaCallback? = null

    interface MediaCallback {
        fun onMediaInfoChanged(info: MediaInfo?)
    }

    data class MediaInfo(
        val title: String,
        val artist: String,
        val isPlaying: Boolean
    )

    fun startListening(callback: MediaCallback) {
        this.callback = callback

        mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager

        val componentName = ComponentName(context, MediaNotificationListener::class.java)

        try {
            val controllers = mediaSessionManager?.getActiveSessions(componentName) ?: emptyList()
            updateActiveController(controllers)

            mediaSessionManager?.addOnActiveSessionsChangedListener({ controllers ->
                updateActiveController(controllers)
            }, componentName)
        } catch (e: SecurityException) {
            // User hasn't granted notification listener permission
            callback.onMediaInfoChanged(null)
        }
    }

    private fun updateActiveController(controllers: List<MediaController>?) {
        activeController?.unregisterCallback(controllerCallback)

        activeController = controllers?.firstOrNull()
        activeController?.registerCallback(controllerCallback)

        updateMediaInfo()
    }

    private val controllerCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updateMediaInfo()
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updateMediaInfo()
        }
    }

    private fun updateMediaInfo() {
        val controller = activeController
        if (controller == null) {
            callback?.onMediaInfoChanged(null)
            return
        }

        val metadata = controller.metadata
        val playbackState = controller.playbackState

        if (metadata == null) {
            callback?.onMediaInfoChanged(null)
            return
        }

        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Unknown"
        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "Unknown Artist"
        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING

        callback?.onMediaInfoChanged(MediaInfo(title, artist, isPlaying))
    }

    fun stopListening() {
        activeController?.unregisterCallback(controllerCallback)
        callback = null
    }

    fun getCurrentMediaInfo(): MediaInfo? {
        val controller = activeController ?: return null
        val metadata = controller.metadata ?: return null
        val playbackState = controller.playbackState

        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE) ?: return null
        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "Unknown Artist"
        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING

        return MediaInfo(title, artist, isPlaying)
    }
}
