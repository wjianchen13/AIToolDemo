package com.example.aitooldemo.test1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
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
        applyBackgroundGrayFilter();
    }

    public void setBadgeImageResource(@DrawableRes int drawableResId) {
        loadBadgeImage(drawableResId);
    }

    public void setBadgeImageUrl(@Nullable String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        loadBadgeImage(url);
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
        clearCurrentRequests();
        final int[] overrideSize = resolveOverrideSize();

        progressTarget = new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                if (!isCurrentRequest(currentRequestSerial)) {
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
            public void onLoadCleared(@Nullable Drawable placeholder) {
                if (!isCurrentRequest(currentRequestSerial)) {
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
                .apply(new RequestOptions().override(overrideSize[0], overrideSize[1]))
                .into(progressTarget);
    }

    private boolean isCurrentRequest(long serial) {
        return requestSerial == serial;
    }

    private void applyBackgroundGrayFilter() {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0f);
        ivBackground.setColorFilter(new ColorMatrixColorFilter(matrix));
    }

    private int[] resolveOverrideSize() {
        int w = getLayoutParams() != null ? getLayoutParams().width : 0;
        int h = getLayoutParams() != null ? getLayoutParams().height : 0;

        if (w <= 0) {
            w = getWidth() > 0 ? getWidth() : ivProgress.getWidth();
        }
        if (h <= 0) {
            h = getHeight() > 0 ? getHeight() : ivProgress.getHeight();
        }

        if (w <= 0 || h <= 0) {
            int screenW = getResources().getDisplayMetrics().widthPixels;
            int screenH = getResources().getDisplayMetrics().heightPixels;
            // Fallback to screen bounds instead of hardcoded dp.
            w = Math.max(1, screenW);
            h = Math.max(1, screenH);
        }

        return new int[]{w, h};
    }

    @Override
    protected void onDetachedFromWindow() {
        clearCurrentRequests();
        super.onDetachedFromWindow();
    }
}
