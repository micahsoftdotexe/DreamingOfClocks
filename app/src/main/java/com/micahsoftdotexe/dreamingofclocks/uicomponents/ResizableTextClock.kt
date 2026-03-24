package com.micahsoftdotexe.dreamingofclocks.uicomponents

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.Layout.Alignment
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.TextClock

class ResizeableTextClock @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : TextClock(context, attrs, defStyleAttr) {

    companion object {
        const val MIN_TEXT_SIZE = 20f
        private const val mEllipsis = "..."
    }

    interface OnTextResizeListener {
        fun onTextResize(textClock: TextClock, oldSize: Float, newSize: Float)
    }

    var onTextResizeListener: OnTextResizeListener? = null
    private val measurePaint = TextPaint()
    private var mNeedsResize = false
    private var mTextSize: Float = textSize
    private var mSavedTypeface: Typeface? = null

    var maxTextSize: Float = 0f
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    var minTextSize: Float = MIN_TEXT_SIZE
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    private var mSpacingMult = 1.0f
    private var mSpacingAdd = 0.0f
    var addEllipsis: Boolean = true

    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, after: Int) {
        super.onTextChanged(text, start, before, after)
        mNeedsResize = true
        requestLayout()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (w != oldw || h != oldh) {
            mNeedsResize = true
        }
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun setTextSize(size: Float) {
        super.setTextSize(size)
        mTextSize = textSize
    }

    override fun setTextSize(unit: Int, size: Float) {
        super.setTextSize(unit, size)
        mTextSize = textSize
    }

    override fun setLineSpacing(add: Float, mult: Float) {
        super.setLineSpacing(add, mult)
        mSpacingAdd = add
        mSpacingMult = mult
    }

    override fun setTypeface(tf: Typeface?) {
        mSavedTypeface = tf
        super.setTypeface(tf)
    }

//    fun resetTextSize() {
//        if (mTextSize > 0f) {
//            super.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize)
//            maxTextSize = mTextSize
//        }
//    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        if (changed || mNeedsResize) {
            val widthLimit =
                right - left - compoundPaddingLeft - compoundPaddingRight
            val heightLimit =
                bottom - top - compoundPaddingTop - compoundPaddingBottom
            resizeText(widthLimit, heightLimit)
        }
        super.onLayout(changed, left, top, right, bottom)
    }

    @SuppressLint("SetTextI18n")
    fun resizeText(width: Int, height: Int) {
        val text = text

        if (text.isNullOrEmpty() || height <= 0 || width <= 0 || mTextSize == 0f) {
            return
        }

        // Reuse measurePaint to avoid allocations each frame
        measurePaint.set(paint)
        measurePaint.isAntiAlias = true
        measurePaint.isSubpixelText = true
        measurePaint.isFakeBoldText = false

        val oldTextSize = measurePaint.textSize
        val hi = if (maxTextSize > 0) minOf(mTextSize, maxTextSize) else mTextSize
        var targetTextSize = hi

        // Binary search for the largest size that fits within height
        if (getTextHeight(text, measurePaint, width, hi) > height) {
            var lo = minTextSize
            var high = hi
            while (high - lo > 1f) {
                val mid = (lo + high) / 2f
                if (getTextHeight(text, measurePaint, width, mid) > height) {
                    high = mid
                } else {
                    lo = mid
                }
            }
            targetTextSize = lo
        }

        if (addEllipsis && targetTextSize == minTextSize
            && getTextHeight(text, measurePaint, width, targetTextSize) > height
        ) {
            measurePaint.textSize = targetTextSize

            val layout = StaticLayout.Builder.obtain(text, 0, text.length, measurePaint, width)
                .setAlignment(Alignment.ALIGN_NORMAL)
                .setLineSpacing(mSpacingAdd, mSpacingMult)
                .setIncludePad(false)
                .build()

            if (layout.lineCount > 0) {
                val lastLine = layout.getLineForVertical(height) - 1

                if (lastLine < 0) {
                    setText("")
                } else {
                    val start = layout.getLineStart(lastLine)
                    var end = layout.getLineEnd(lastLine)
                    var lineWidth = layout.getLineWidth(lastLine)
                    val ellipseWidth = measurePaint.measureText(mEllipsis)

                    while (width < lineWidth + ellipseWidth && end > start) {
                        end--
                        lineWidth = measurePaint.measureText(
                            text.subSequence(start, end + 1).toString()
                        )
                    }
                    setText(text.subSequence(0, end).toString() + mEllipsis)
                }
            }
        }

        // Apply final size to the view paint
        setTextSize(TypedValue.COMPLEX_UNIT_PX, targetTextSize)
        setLineSpacing(mSpacingAdd, mSpacingMult)
        // Re-apply custom typeface — setTextSize can drop it on some Android versions
        mSavedTypeface?.let { paint.typeface = it }

        onTextResizeListener?.onTextResize(this, oldTextSize, targetTextSize)
        mNeedsResize = false
    }

    private fun getTextHeight(
        source: CharSequence,
        paint: TextPaint,
        width: Int,
        textSize: Float
    ): Int {
        paint.textSize = textSize

        val layout = StaticLayout.Builder.obtain(source, 0, source.length, paint, width)
            .setAlignment(Alignment.ALIGN_NORMAL)
            .setLineSpacing(mSpacingAdd, mSpacingMult)
            .setIncludePad(true)
            .build()

        return layout.height
    }
}