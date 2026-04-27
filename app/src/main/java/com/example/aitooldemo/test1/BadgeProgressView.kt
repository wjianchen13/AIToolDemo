package com.example.aitooldemo.test1

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.content.res.TypedArray
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView

import androidx.annotation.DrawableRes
import androidx.core.view.ViewCompat

import com.example.aitooldemo.R
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlin.math.max
import kotlin.math.min

class BadgeProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    FrameLayout(context, attrs, defStyleAttr) {
    private val ivBackground: ImageView
    private val ivProgress: ImageView
    private val requestManager: RequestManager = Glide.with(context)

    private var hasImage = false
    private var progress = 0
    private var imageWidth = 0
    private var imageHeight = 0
    private var progressDirection = DIRECTION_AUTO
    private var requestSerial = 0L
    private var decodeWidthPx = 0
    private var decodeHeightPx = 0
    private var hasExplicitDecodeSize = false
    private var lastModel: Any? = null

    private var progressTarget: CustomTarget<Bitmap>? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_badge_progress, this, true)
        ivBackground = findViewById<ImageView>(R.id.ivBackground)
        ivProgress = findViewById<ImageView>(R.id.ivProgress)
        val defaultSize: Int = getResources().getDimensionPixelSize(R.dimen.badge_progress_size)
        setDecodeSizePx(defaultSize, defaultSize)
        applyDecodeAttrs(context, attrs)
        applyBackgroundGrayFilter()
    }

    fun setBadgeImageResource(@DrawableRes drawableResId: Int) {
        lastModel = drawableResId
        loadBadgeImage(drawableResId)
    }

    fun setBadgeImageUrl(url: String?) {
        if (TextUtils.isEmpty(url)) {
            lastModel = null
            resetContent()
            return
        }
        lastModel = url
        loadBadgeImage(url!!)
    }

    fun setDecodeSizePx(sizePx: Int) {
        setDecodeSizePx(sizePx, sizePx)
    }

    fun setDecodeSizePx(widthPx: Int, heightPx: Int) {
        val screenW: Int = getResources().getDisplayMetrics().widthPixels
        val screenH: Int = getResources().getDisplayMetrics().heightPixels
        decodeWidthPx = max(1.0, min(widthPx.toDouble(), screenW.toDouble())).toInt()
        decodeHeightPx = max(1.0, min(heightPx.toDouble(), screenH.toDouble())).toInt()
    }

    fun setProgress(progress: Int) {
        this.progress = max(0.0, min(100.0, progress.toDouble())).toInt()
        updateProgressClip()
    }

    fun getProgress(): Int {
        return progress
    }

    fun setProgressDirection(direction: Int) {
        if (direction != DIRECTION_AUTO && direction != DIRECTION_LTR && direction != DIRECTION_RTL) {
            return
        }
        progressDirection = direction
        updateProgressClip()
    }

    private fun updateProgressClip() {
        if (!hasImage) {
            return
        }

        var fullWidth = imageWidth
        var height = imageHeight
        if (ivProgress.width > 0 && ivProgress.height > 0) {
            fullWidth = ivProgress.width
            height = ivProgress.height
        }
        var cropWidth = Math.round((fullWidth * progress) / 100f)
        cropWidth = max(0.0, min(cropWidth.toDouble(), fullWidth.toDouble())).toInt()

        if (cropWidth == 0) {
            ivProgress.clipBounds = Rect(0, 0, 0, height)
            return
        }

        val isRtl = isProgressRtl
        val clipRect = if (isRtl) {
            Rect(fullWidth - cropWidth, 0, fullWidth, height)
        } else {
            Rect(0, 0, cropWidth, height)
        }
        ivProgress.clipBounds = clipRect
    }

    private val isProgressRtl: Boolean
        get() {
            if (progressDirection == DIRECTION_RTL) {
                return true
            }
            if (progressDirection == DIRECTION_LTR) {
                return false
            }
            return ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL
        }

    private fun clearCurrentRequests() {
        progressTarget?.let { requestManager.clear(it) }
        progressTarget = null
    }

    private fun resetContent() {
        // Invalidate all inflight callbacks to prevent stale resources from being set.
        requestSerial++
        clearCurrentRequests()
        hasImage = false
        imageWidth = 0
        imageHeight = 0
        ivProgress.setImageDrawable(null)
        ivBackground.setImageDrawable(null)
        ivProgress.clipBounds = null
    }

    private fun loadBadgeImage(model: Any) {
        requestSerial++
        val currentRequestSerial = requestSerial
        // Keep current drawable until new resource is ready to avoid visible flicker.
        clearCurrentRequests()
        startLoadWhenSizeReady(model, currentRequestSerial)
    }

    private fun isCurrentRequest(serial: Long): Boolean {
        return requestSerial == serial
    }

    private fun applyBackgroundGrayFilter() {
        val matrix: ColorMatrix = ColorMatrix()
        matrix.setSaturation(0f)
        ivBackground.setColorFilter(ColorMatrixColorFilter(matrix))
    }

    private fun startLoadWhenSizeReady(model: Any, serial: Long) {
        if (!isCurrentRequest(serial)) {
            return
        }

        val target = object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                if (!isCurrentRequest(serial)) {
                    return
                }
                hasImage = true
                imageWidth = resource.width
                imageHeight = resource.height
                ivProgress.setImageBitmap(resource)
                ivBackground.setImageBitmap(resource)
                ivProgress.post { this@BadgeProgressView.updateProgressClip() }
                requestLayout()
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                if (!isCurrentRequest(serial)) {
                    return
                }
                hasImage = false
                imageWidth = 0
                imageHeight = 0
                ivProgress.setImageDrawable(errorDrawable)
                ivBackground.setImageDrawable(errorDrawable)
                ivProgress.clipBounds = null
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                if (!isCurrentRequest(serial)) {
                    return
                }
                hasImage = false
                imageWidth = 0
                imageHeight = 0
                ivProgress.setImageDrawable(placeholder)
                ivBackground.setImageDrawable(placeholder)
            }
        }
        progressTarget = target

        requestManager
            .asBitmap()
            .load(model)
            .apply(RequestOptions().override(decodeWidthPx, decodeHeightPx))
            .into(target)
    }

    private fun applyDecodeAttrs(context: Context, attrs: AttributeSet?) {
        if (attrs == null) {
            return
        }
        val ta: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.BadgeProgressView)
        val decodeSize: Int =
            ta.getDimensionPixelSize(R.styleable.BadgeProgressView_bpvDecodeSize, -1)
        val decodeWidth: Int =
            ta.getDimensionPixelSize(R.styleable.BadgeProgressView_bpvDecodeWidth, -1)
        val decodeHeight: Int =
            ta.getDimensionPixelSize(R.styleable.BadgeProgressView_bpvDecodeHeight, -1)
        ta.recycle()

        if (decodeSize > 0) {
            setDecodeSizePx(decodeSize, decodeSize)
            hasExplicitDecodeSize = true
        }
        if (decodeWidth > 0 || decodeHeight > 0) {
            val finalW = if (decodeWidth > 0) decodeWidth else decodeWidthPx
            val finalH = if (decodeHeight > 0) decodeHeight else decodeHeightPx
            setDecodeSizePx(finalW, finalH)
            hasExplicitDecodeSize = true
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w != oldw || h != oldh) {
            // If decode size is not explicitly specified, keep it in sync with final render size.
            if (!hasExplicitDecodeSize && w > 0 && h > 0 && (decodeWidthPx != w || decodeHeightPx != h)) {
                setDecodeSizePx(w, h)
                if (lastModel != null) {
                    loadBadgeImage(lastModel!!)
                    return
                }
            }
            updateProgressClip()
        }
    }

    override fun onDetachedFromWindow() {
        resetContent()
        super.onDetachedFromWindow()
    }

    companion object {
        const val DIRECTION_AUTO: Int = 0
        const val DIRECTION_LTR: Int = 1
        const val DIRECTION_RTL: Int = 2
    }
}
