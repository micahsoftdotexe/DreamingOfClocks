package com.micahsoftdotexe.dreamingofclocks.services.screensaver

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import com.micahsoftdotexe.dreamingofclocks.utils.MediaMetadataHelper
import androidx.core.view.isVisible

class MediaDisplayManager {

    private var mediaHelper: MediaMetadataHelper? = null
    private val handler = Handler(Looper.getMainLooper())
    private var mediaTextView: TextView? = null

    private val playIcon = "▶"
    private val pauseIcon = "⏸"

    private val playPauseAnimationRunnable = object : Runnable {
        override fun run() {
            val tv = mediaTextView ?: return
            if (tv.isVisible) {
                updateMediaDisplay()
                handler.postDelayed(this, 500)
            }
        }
    }

    fun setup(context: Context, mediaText: TextView) {
        mediaTextView = mediaText
        mediaHelper = MediaMetadataHelper(context).apply {
            startListening(object : MediaMetadataHelper.MediaCallback {
                override fun onMediaInfoChanged(info: MediaMetadataHelper.MediaInfo?) {
                    handler.post {
                        updateMediaDisplay(info)
                    }
                }
            })
        }
        handler.post(playPauseAnimationRunnable)
    }

    fun teardown() {
        mediaHelper?.stopListening()
        mediaHelper = null
        handler.removeCallbacks(playPauseAnimationRunnable)
        mediaTextView = null
    }

    private fun updateMediaDisplay(info: MediaMetadataHelper.MediaInfo? = null) {
        val tv = mediaTextView ?: return
        val mediaInfo = info ?: mediaHelper?.getCurrentMediaInfo()

        if (mediaInfo == null) {
            tv.visibility = View.GONE
            return
        }

        val icon = if (mediaInfo.isPlaying) playIcon else pauseIcon
        tv.text = "$icon ${mediaInfo.title} - ${mediaInfo.artist}"
        tv.visibility = View.VISIBLE
    }
}
