package com.micahsoftdotexe.dreamingofclocks.services.screensaver

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.micahsoftdotexe.dreamingofclocks.R
import com.micahsoftdotexe.dreamingofclocks.uicomponents.weatherbackground.WeatherBackgroundView
import com.micahsoftdotexe.dreamingofclocks.weather.WeatherCacheStore
import com.micahsoftdotexe.dreamingofclocks.weather.WeatherUpdateScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class WeatherBackgroundController(
    private val weatherCache: WeatherCacheStore,
    private val weatherUpdateScheduler: WeatherUpdateScheduler
) {
    private var weatherScope: CoroutineScope? = null
    private var weatherView: WeatherBackgroundView? = null

    fun setup(context: Context, rootView: View, config: PreferencesManager.ScreensaverConfig) {
        val bgView = WeatherBackgroundView(context)
        weatherView = bgView

        // Show cached weather immediately
        val cached = weatherCache.load(context)
        if (cached != null) {
            bgView.setWeather(cached.condition, cached.isDay)
        }

        // Insert the weather view behind all other content
        when (rootView) {
            is FrameLayout -> {
                rootView.addView(bgView, 0, FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ))
            }
            is LinearLayout -> {
                val parent = rootView.parent as? ViewGroup
                if (parent != null) {
                    val index = parent.indexOfChild(rootView)
                    parent.removeView(rootView)
                    val wrapper = FrameLayout(context).apply {
                        id = R.id.screensaver_root
                    }
                    wrapper.addView(bgView, FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    ))
                    wrapper.addView(rootView, FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    ))
                    parent.addView(wrapper, index)
                }
            }
        }

        // Start periodic weather updates
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        weatherScope = scope
        weatherUpdateScheduler.startPeriodicUpdates(context, scope) { data ->
            bgView.setWeather(data.condition, data.isDay)
        }
    }

    fun teardown() {
        weatherScope?.cancel()
        weatherScope = null
        weatherView = null
    }
}
