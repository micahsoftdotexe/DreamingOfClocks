package com.micahsoftdotexe.dreamingofclocks.uicomponents

import android.content.Context
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
    private var mNeedsResize = false
    private var mTextSize: Float = textSize

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

    fun resetTextSize() {
        if (mTextSize > 0f) {
            super.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize)
            maxTextSize = mTextSize
        }
    }

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

    fun resizeText(width: Int, height: Int) {
        val text = text

        if (text.isNullOrEmpty() || height <= 0 || width <= 0 || mTextSize == 0f) {
            return
        }

        // Use a copy of the current paint for measurement so we do not modify the view paint.
        // Ensure anti-aliasing and subpixel text are enabled and disable fake bold.
        val textPaint = TextPaint(paint).apply {
            isAntiAlias = true
            isSubpixelText = true
            isFakeBoldText = false
        }

        val oldTextSize = textPaint.textSize
        var targetTextSize =
            if (maxTextSize > 0) minOf(mTextSize, maxTextSize) else mTextSize

        var textHeight = getTextHeight(text, textPaint, width, targetTextSize)

        while (textHeight > height && targetTextSize > minTextSize) {
            targetTextSize = maxOf(targetTextSize - 2f, minTextSize)
            textHeight = getTextHeight(text, textPaint, width, targetTextSize)
        }

        if (addEllipsis && targetTextSize == minTextSize && textHeight > height) {
            val paintCopy = TextPaint(textPaint)

            val layout = StaticLayout.Builder.obtain(text, 0, text.length, paintCopy, width)
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
                    val ellipseWidth = paintCopy.measureText(mEllipsis)

                    while (width < lineWidth + ellipseWidth && end > start) {
                        end--
                        lineWidth = paintCopy.measureText(
                            text.subSequence(start, end + 1).toString()
                        )
                    }
                    setText(text.subSequence(0, end).toString() + mEllipsis)
                }
            }
        }

        // Apply final size to the view paint (keeps the same typeface and flags)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, targetTextSize)
        setLineSpacing(mSpacingAdd, mSpacingMult)

        onTextResizeListener?.onTextResize(this, oldTextSize, targetTextSize)
        mNeedsResize = false
    }

    private fun getTextHeight(
        source: CharSequence,
        paint: TextPaint,
        width: Int,
        textSize: Float
    ): Int {
        val paintCopy = TextPaint(paint)
        paintCopy.textSize = textSize

        val layout = StaticLayout.Builder.obtain(source, 0, source.length, paintCopy, width)
            .setAlignment(Alignment.ALIGN_NORMAL)
            .setLineSpacing(mSpacingAdd, mSpacingMult)
            .setIncludePad(true)
            .build()

        return layout.height
    }
}