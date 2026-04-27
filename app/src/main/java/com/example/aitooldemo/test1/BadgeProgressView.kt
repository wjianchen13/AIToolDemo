package com.example.aitooldemo.test1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import com.example.aitooldemo.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

public class BadgeProgressView extends FrameLayout {

    public static final int DIRECTION_AUTO = 0;
    public static final int DIRECTION_LTR = 1;
    public static final int DIRECTION_RTL = 2;

    private final ImageView ivBackground;
    private final ImageView ivProgress;
    private final RequestManager requestManager;

    private boolean hasImage = false;
    private int progress = 0;
    private int imageWidth = 0;
    private int imageHeight = 0;
    private int progressDirection = DIRECTION_AUTO;
    private long requestSerial = 0L;
    private int decodeWidthPx;
    private int decodeHeightPx;
    private boolean hasExplicitDecodeSize = false;
    @Nullable
    private Object lastModel;

    private CustomTarget<Bitmap> progressTarget;

    public BadgeProgressView(@NonNull Context context) {
        this(context, null);
    }

    public BadgeProgressView(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BadgeProgressView(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        requestManager = Glide.with(context);
        LayoutInflater.from(context).inflate(R.layout.view_badge_progress, this, true);
        ivBackground = findViewById(R.id.ivBackground);
        ivProgress = findViewById(R.id.ivProgress);
        int defaultSize = getResources().getDimensionPixelSize(R.dimen.badge_progress_size);
        setDecodeSizePx(defaultSize, defaultSize);
        applyDecodeAttrs(context, attrs);
        applyBackgroundGrayFilter();
    }

    public void setBadgeImageResource(@DrawableRes int drawableResId) {
        lastModel = drawableResId;
        loadBadgeImage(drawableResId);
    }

    public void setBadgeImageUrl(@Nullable String url) {
        if (TextUtils.isEmpty(url)) {
            lastModel = null;
            resetContent();
            return;
        }
        lastModel = url;
        loadBadgeImage(url);
    }

    public void setDecodeSizePx(int sizePx) {
        setDecodeSizePx(sizePx, sizePx);
    }

    public void setDecodeSizePx(int widthPx, int heightPx) {
        int screenW = getResources().getDisplayMetrics().widthPixels;
        int screenH = getResources().getDisplayMetrics().heightPixels;
        decodeWidthPx = Math.max(1, Math.min(widthPx, screenW));
        decodeHeightPx = Math.max(1, Math.min(heightPx, screenH));
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress));
        updateProgressClip();
    }

    public int getProgress() {
        return progress;
    }

    public void setProgressDirection(int direction) {
        if (direction != DIRECTION_AUTO
                && direction != DIRECTION_LTR
                && direction != DIRECTION_RTL) {
            return;
        }
        progressDirection = direction;
        updateProgressClip();
    }

    private void updateProgressClip() {
        if (!hasImage) {
            return;
        }

        int fullWidth = imageWidth;
        int height = imageHeight;
        if (ivProgress.getWidth() > 0 && ivProgress.getHeight() > 0) {
            fullWidth = ivProgress.getWidth();
            height = ivProgress.getHeight();
        }
        int cropWidth = Math.round((fullWidth * progress) / 100f);
        cropWidth = Math.max(0, Math.min(cropWidth, fullWidth));

        if (cropWidth == 0) {
            ivProgress.setClipBounds(new Rect(0, 0, 0, height));
            return;
        }

        boolean isRtl = isProgressRtl();
        Rect clipRect;
        if (isRtl) {
            clipRect = new Rect(fullWidth - cropWidth, 0, fullWidth, height);
        } else {
            clipRect = new Rect(0, 0, cropWidth, height);
        }
        ivProgress.setClipBounds(clipRect);
    }

    private boolean isProgressRtl() {
        if (progressDirection == DIRECTION_RTL) {
            return true;
        }
        if (progressDirection == DIRECTION_LTR) {
            return false;
        }
        return ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    private void clearCurrentRequests() {
        if (progressTarget != null) {
            requestManager.clear(progressTarget);
            progressTarget = null;
        }
    }

    private void resetContent() {
        // Invalidate all inflight callbacks to prevent stale resources from being set.
        requestSerial++;
        clearCurrentRequests();
        hasImage = false;
        imageWidth = 0;
        imageHeight = 0;
        ivProgress.setImageDrawable(null);
        ivBackground.setImageDrawable(null);
        ivProgress.setClipBounds(null);
    }

    private void loadBadgeImage(@NonNull Object model) {
        requestSerial++;
        final long currentRequestSerial = requestSerial;
        // Keep current drawable until new resource is ready to avoid visible flicker.
        clearCurrentRequests();
        startLoadWhenSizeReady(model, currentRequestSerial);
    }

    private boolean isCurrentRequest(long serial) {
        return requestSerial == serial;
    }

    private void applyBackgroundGrayFilter() {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0f);
        ivBackground.setColorFilter(new ColorMatrixColorFilter(matrix));
    }

    private void startLoadWhenSizeReady(@NonNull Object model, long serial) {
        if (!isCurrentRequest(serial)) {
            return;
        }

        progressTarget = new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                if (!isCurrentRequest(serial)) {
                    return;
                }
                hasImage = true;
                imageWidth = resource.getWidth();
                imageHeight = resource.getHeight();
                ivProgress.setImageBitmap(resource);
                ivBackground.setImageBitmap(resource);
                ivProgress.post(BadgeProgressView.this::updateProgressClip);
                requestLayout();
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                if (!isCurrentRequest(serial)) {
                    return;
                }
                hasImage = false;
                imageWidth = 0;
                imageHeight = 0;
                ivProgress.setImageDrawable(errorDrawable);
                ivBackground.setImageDrawable(errorDrawable);
                ivProgress.setClipBounds(null);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                if (!isCurrentRequest(serial)) {
                    return;
                }
                hasImage = false;
                imageWidth = 0;
                imageHeight = 0;
                ivProgress.setImageDrawable(placeholder);
                ivBackground.setImageDrawable(placeholder);
            }
        };

        requestManager
                .asBitmap()
                .load(model)
                .apply(new RequestOptions().override(decodeWidthPx, decodeHeightPx))
                .into(progressTarget);
    }

    private void applyDecodeAttrs(@NonNull Context context, @Nullable AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.BadgeProgressView);
        int decodeSize = ta.getDimensionPixelSize(R.styleable.BadgeProgressView_bpvDecodeSize, -1);
        int decodeWidth = ta.getDimensionPixelSize(R.styleable.BadgeProgressView_bpvDecodeWidth, -1);
        int decodeHeight = ta.getDimensionPixelSize(R.styleable.BadgeProgressView_bpvDecodeHeight, -1);
        ta.recycle();

        if (decodeSize > 0) {
            setDecodeSizePx(decodeSize, decodeSize);
            hasExplicitDecodeSize = true;
        }
        if (decodeWidth > 0 || decodeHeight > 0) {
            int finalW = decodeWidth > 0 ? decodeWidth : decodeWidthPx;
            int finalH = decodeHeight > 0 ? decodeHeight : decodeHeightPx;
            setDecodeSizePx(finalW, finalH);
            hasExplicitDecodeSize = true;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw || h != oldh) {
            // If decode size is not explicitly specified, keep it in sync with final render size.
            if (!hasExplicitDecodeSize && w > 0 && h > 0 && (decodeWidthPx != w || decodeHeightPx != h)) {
                setDecodeSizePx(w, h);
                if (lastModel != null) {
                    loadBadgeImage(lastModel);
                    return;
                }
            }
            updateProgressClip();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        resetContent();
        super.onDetachedFromWindow();
    }
}
