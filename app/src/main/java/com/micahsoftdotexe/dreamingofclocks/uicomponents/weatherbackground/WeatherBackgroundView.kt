package com.micahsoftdotexe.dreamingofclocks.uicomponents.weatherbackground

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.AttributeSet
import android.view.View
import com.micahsoftdotexe.dreamingofclocks.weather.WeatherCondition
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class WeatherBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var condition = WeatherCondition.CLEAR
    private var isDay = true
    private var frameCount = 0L

    private var isPowerSaveMode = false
    private val powerManager by lazy { context.getSystemService(Context.POWER_SERVICE) as PowerManager }
    private val powerSaveReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            isPowerSaveMode = powerManager.isPowerSaveMode
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            frameCount++
            invalidate()
            handler.postDelayed(this, getFrameDelay())
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Particle pools
    private val rainDrops = mutableListOf<Particle>()
    private val snowFlakes = mutableListOf<Particle>()
    private val stars = mutableListOf<Particle>()
    private val clouds = mutableListOf<CloudData>()
    private val fogBands = mutableListOf<FogBand>()

    // Lightning state
    private var lightningAlpha = 0f
    private var nextLightningFrame = Random.nextInt(90, 240) // 3-8 seconds at 30fps

    private var particlesInitialized = false

    // Cached sky gradient
    private var cachedGradient: LinearGradient? = null
    private var cachedGradientHeight = 0
    private var cachedGradientCondition: WeatherCondition? = null
    private var cachedGradientIsDay: Boolean? = null

    fun setWeather(condition: WeatherCondition, isDay: Boolean) {
        this.condition = condition
        this.isDay = isDay
        particlesInitialized = false
        cachedGradient = null
        invalidate()
    }

    private fun getFrameDelay(): Long {
        val baseFps = when (condition) {
            WeatherCondition.CLEAR -> 66L                          // 15fps: slow sun rays / star twinkle
            WeatherCondition.FOG -> 66L                            // 15fps: slow fog drift
            WeatherCondition.PARTLY_CLOUDY, WeatherCondition.CLOUDY -> 50L  // 20fps: cloud drift
            WeatherCondition.RAIN, WeatherCondition.SNOW, WeatherCondition.THUNDERSTORM -> 33L // 30fps: fast particles
        }
        return if (isPowerSaveMode) baseFps * 2 else baseFps
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isPowerSaveMode = powerManager.isPowerSaveMode
        context.registerReceiver(powerSaveReceiver, IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED))
        handler.post(updateRunnable)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(updateRunnable)
        context.unregisterReceiver(powerSaveReceiver)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        particlesInitialized = false
        cachedGradient = null
    }

    private fun initParticles() {
        if (width == 0 || height == 0) return
        particlesInitialized = true

        rainDrops.clear()
        snowFlakes.clear()
        stars.clear()
        clouds.clear()
        fogBands.clear()

        when (condition) {
            WeatherCondition.RAIN, WeatherCondition.THUNDERSTORM -> {
                repeat(120) {
                    rainDrops.add(Particle(
                        x = Random.nextFloat() * width,
                        y = Random.nextFloat() * height,
                        speed = 8f + Random.nextFloat() * 6f,
                        size = 2f + Random.nextFloat() * 2f,
                        phase = Random.nextFloat() * Math.PI.toFloat() * 2
                    ))
                }
                addClouds(4)
            }
            WeatherCondition.SNOW -> {
                repeat(80) {
                    snowFlakes.add(Particle(
                        x = Random.nextFloat() * width,
                        y = Random.nextFloat() * height,
                        speed = 1f + Random.nextFloat() * 2f,
                        size = 2f + Random.nextFloat() * 4f,
                        phase = Random.nextFloat() * Math.PI.toFloat() * 2
                    ))
                }
                addClouds(3)
            }
            WeatherCondition.CLOUDY -> addClouds(5)
            WeatherCondition.PARTLY_CLOUDY -> addClouds(3)
            WeatherCondition.CLEAR -> {
                if (isDay) {
                    addClouds(2) // small decorative clouds
                } else {
                    repeat(60) {
                        stars.add(Particle(
                            x = Random.nextFloat() * width,
                            y = Random.nextFloat() * height * 0.7f,
                            speed = 0f,
                            size = 1f + Random.nextFloat() * 2f,
                            phase = Random.nextFloat() * Math.PI.toFloat() * 2
                        ))
                    }
                }
            }
            WeatherCondition.FOG -> {
                repeat(6) { i ->
                    fogBands.add(FogBand(
                        y = height * (0.1f + i * 0.15f),
                        alpha = 80 + Random.nextInt(60),
                        speed = 0.3f + Random.nextFloat() * 0.4f,
                        phase = Random.nextFloat() * Math.PI.toFloat() * 2
                    ))
                }
            }
        }
    }

    private fun addClouds(count: Int) {
        repeat(count) { _ ->
            clouds.add(CloudData(
                x = Random.nextFloat() * width,
                y = height * (0.05f + Random.nextFloat() * 0.2f),
                width = width * (0.25f + Random.nextFloat() * 0.2f),
                speed = 0.3f + Random.nextFloat() * 0.5f
            ))
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (!particlesInitialized) initParticles()
        if (width == 0 || height == 0) return

        drawSky(canvas)

        when (condition) {
            WeatherCondition.CLEAR -> {
                if (isDay) {
                    drawSun(canvas)
                    drawClouds(canvas, decorative = true)
                } else {
                    drawStars(canvas)
                    drawMoon(canvas)
                }
            }
            WeatherCondition.PARTLY_CLOUDY -> {
                if (isDay) drawSun(canvas) else { drawStars(canvas); drawMoon(canvas) }
                drawClouds(canvas)
            }
            WeatherCondition.CLOUDY -> drawClouds(canvas)
            WeatherCondition.FOG -> drawFog(canvas)
            WeatherCondition.RAIN -> {
                drawClouds(canvas)
                drawRain(canvas)
            }
            WeatherCondition.THUNDERSTORM -> {
                drawClouds(canvas)
                drawRain(canvas)
                drawLightning(canvas)
            }
            WeatherCondition.SNOW -> {
                if (!isDay) { drawStars(canvas); drawMoon(canvas) }
                drawClouds(canvas)
                drawSnow(canvas)
            }
        }
    }

    private fun drawSky(canvas: Canvas) {
        if (cachedGradient == null || cachedGradientHeight != height
            || cachedGradientCondition != condition || cachedGradientIsDay != isDay) {
            val (topColor, bottomColor) = getSkyColors()
            cachedGradient = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                topColor, bottomColor, Shader.TileMode.CLAMP
            )
            cachedGradientHeight = height
            cachedGradientCondition = condition
            cachedGradientIsDay = isDay
        }
        paint.shader = cachedGradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null
    }

    private fun getSkyColors(): Pair<Int, Int> = when (condition) {
        WeatherCondition.CLEAR -> if (isDay) Pair(0xFF87CEEB.toInt(), 0xFF4DA6E8.toInt())
            else Pair(0xFF0B1026.toInt(), 0xFF1A237E.toInt())
        WeatherCondition.PARTLY_CLOUDY -> if (isDay) Pair(0xFF6BB3D9.toInt(), 0xFF3E8EC4.toInt())
            else Pair(0xFF0D1533.toInt(), 0xFF1A237E.toInt())
        WeatherCondition.CLOUDY -> Pair(0xFF8E9EAB.toInt(), 0xFF6C7A89.toInt())
        WeatherCondition.FOG -> Pair(0xFFBDC3C7.toInt(), 0xFF95A5A6.toInt())
        WeatherCondition.RAIN -> Pair(0xFF4A5568.toInt(), 0xFF2D3748.toInt())
        WeatherCondition.THUNDERSTORM -> Pair(0xFF1A202C.toInt(), 0xFF171923.toInt())
        WeatherCondition.SNOW -> if (isDay) Pair(0xFFA8C4D6.toInt(), 0xFF7BA3BE.toInt())
            else Pair(0xFF1A2340.toInt(), 0xFF0D1533.toInt())
    }

    private fun drawSun(canvas: Canvas) {
        var cx = width * 0.8f
        var cy = height * 0.15f
        val radius = width * 0.06f

        // Keep sun + glow inside a safe circular area for round/small screens
        val safeRadius = min(width, height) / 2f * 0.85f
        val dx = cx - width / 2f
        val dy = cy - height / 2f
        val dist = sqrt(dx * dx + dy * dy)
        val elementRadius = radius * 2.5f // glow radius
        if (dist + elementRadius > safeRadius && dist > 0f) {
            val scale = (safeRadius - elementRadius) / dist
            cx = width / 2f + dx * scale
            cy = height / 2f + dy * scale
        }
        val rayLength = radius * 1.8f

        // Sun glow
        paint.color = 0x40FFEB3B
        paint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy, radius * 2.5f, paint)

        // Sun body
        paint.color = 0xFFFFEB3B.toInt()
        canvas.drawCircle(cx, cy, radius, paint)

        // Rotating rays
        paint.strokeWidth = 3f
        paint.style = Paint.Style.STROKE
        paint.color = 0xFFFFEB3B.toInt()
        val rayAngleOffset = frameCount * 0.02f
        for (i in 0 until 12) {
            val angle = rayAngleOffset + i * (Math.PI / 6).toFloat()
            val startR = radius * 1.3f
            canvas.drawLine(
                cx + cos(angle) * startR, cy + sin(angle) * startR,
                cx + cos(angle) * rayLength, cy + sin(angle) * rayLength,
                paint
            )
        }
        paint.style = Paint.Style.FILL
    }

    private fun drawMoon(canvas: Canvas) {
        var cx = width * 0.8f
        var cy = height * 0.15f
        val radius = width * 0.05f

        // Keep moon + glow inside a safe circular area for round/small screens
        val safeRadius = min(width, height) / 2f * 0.85f
        val dx = cx - width / 2f
        val dy = cy - height / 2f
        val dist = sqrt(dx * dx + dy * dy)
        val elementRadius = radius * 2f // glow radius
        if (dist + elementRadius > safeRadius && dist > 0f) {
            val scale = (safeRadius - elementRadius) / dist
            cx = width / 2f + dx * scale
            cy = height / 2f + dy * scale
        }

        // Moon glow
        paint.color = 0x20FFFFFF
        canvas.drawCircle(cx, cy, radius * 2f, paint)

        // White circle
        paint.color = 0xFFF5F5DC.toInt()
        canvas.drawCircle(cx, cy, radius, paint)

        // Overlapping circle to create crescent
        val skyTop = getSkyColors().first
        paint.color = skyTop
        canvas.drawCircle(cx - radius * 0.35f, cy - radius * 0.1f, radius * 0.85f, paint)
    }

    private fun drawStars(canvas: Canvas) {
        val t = frameCount * 0.05f
        for (star in stars) {
            val alpha = (0.3f + 0.7f * ((sin(t + star.phase) + 1f) / 2f)).coerceIn(0f, 1f)
            paint.color = Color.argb((alpha * 255).toInt(), 255, 255, 255)
            canvas.drawCircle(star.x, star.y, star.size, paint)
        }
    }

    private fun drawClouds(canvas: Canvas, decorative: Boolean = false) {
        val alpha = if (decorative) 140 else 220
        for (cloud in clouds) {
            cloud.x += cloud.speed
            if (cloud.x > width + cloud.width) cloud.x = -cloud.width

            drawSingleCloud(canvas, cloud.x, cloud.y, cloud.width, alpha)
        }
    }

    private fun drawSingleCloud(canvas: Canvas, cx: Float, cy: Float, w: Float, alpha: Int) {
        paint.color = Color.argb(alpha, 230, 230, 240)
        val r = w * 0.2f
        // Three overlapping circles for puffy shape
        canvas.drawCircle(cx - r * 0.8f, cy + r * 0.2f, r * 0.9f, paint)
        canvas.drawCircle(cx, cy - r * 0.2f, r * 1.2f, paint)
        canvas.drawCircle(cx + r * 0.9f, cy + r * 0.15f, r, paint)
    }

    private fun drawRain(canvas: Canvas) {
        paint.color = 0xAAB0C4DE.toInt()
        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE
        for (drop in rainDrops) {
            drop.y += drop.speed
            drop.x += 1.5f // slight wind angle
            if (drop.y > height) {
                drop.y = -10f
                drop.x = Random.nextFloat() * width
            }
            canvas.drawLine(drop.x, drop.y, drop.x + 2f, drop.y + drop.size * 4, paint)
        }
        paint.style = Paint.Style.FILL
    }

    private fun drawSnow(canvas: Canvas) {
        val t = frameCount * 0.03f
        for (flake in snowFlakes) {
            flake.y += flake.speed
            flake.x += sin(t + flake.phase) * 0.8f // wobble
            if (flake.y > height) {
                flake.y = -10f
                flake.x = Random.nextFloat() * width
            }
            val alpha = (180 + 75 * sin(t * 0.5f + flake.phase)).toInt().coerceIn(0, 255)
            paint.color = Color.argb(alpha, 255, 255, 255)
            canvas.drawCircle(flake.x, flake.y, flake.size, paint)
        }
    }

    private fun drawFog(canvas: Canvas) {
        val t = frameCount * 0.02f
        for (band in fogBands) {
            val yOffset = sin(t + band.phase) * 20f
            val y = band.y + yOffset
            paint.color = Color.argb(band.alpha, 200, 200, 210)
            canvas.drawRect(0f, y, width.toFloat(), y + height * 0.08f, paint)
        }
    }

    private fun drawLightning(canvas: Canvas) {
        if (frameCount.toInt() >= nextLightningFrame) {
            lightningAlpha = 0.8f
            nextLightningFrame = frameCount.toInt() + Random.nextInt(90, 240)
        }
        if (lightningAlpha > 0.01f) {
            paint.color = Color.argb((lightningAlpha * 255).toInt(), 255, 255, 255)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            lightningAlpha *= 0.85f
        }
    }

    private data class Particle(
        var x: Float,
        var y: Float,
        val speed: Float,
        val size: Float,
        val phase: Float
    )

    private data class CloudData(
        var x: Float,
        val y: Float,
        val width: Float,
        val speed: Float
    )

    private data class FogBand(
        val y: Float,
        val alpha: Int,
        val speed: Float,
        val phase: Float
    )
}
