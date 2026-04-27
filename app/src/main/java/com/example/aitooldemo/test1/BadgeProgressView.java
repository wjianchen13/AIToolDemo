package com.example.aitooldemo.test1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
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
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

public class BadgeProgressView extends FrameLayout {

    public static final int DIRECTION_AUTO = 0;
    public static final int DIRECTION_LTR = 1;
    public static final int DIRECTION_RTL = 2;

    private final ImageView ivBackground;
    private final ImageView ivProgress;

    private Bitmap sourceBitmap;
    private float progress = 0f;
    private int imageWidth = 0;
    private int imageHeight = 0;
    private int progressDirection = DIRECTION_AUTO;
    private Integer sourceResId;

    private CustomTarget<Bitmap> progressTarget;
    private CustomTarget<Bitmap> backgroundTarget;

    public BadgeProgressView(@NonNull Context context) {
        this(context, null);
    }

    public BadgeProgressView(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BadgeProgressView(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_badge_progress, this, true);
        ivBackground = findViewById(R.id.ivBackground);
        ivProgress = findViewById(R.id.ivProgress);
    }

    public void setBadgeImageResource(@DrawableRes int drawableResId) {
        sourceResId = drawableResId;
        clearCurrentRequests();

        progressTarget = new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                if (sourceResId == null || sourceResId != drawableResId) {
                    return;
                }
                sourceBitmap = resource;
                imageWidth = resource.getWidth();
                imageHeight = resource.getHeight();
                updateImageViewSize(imageWidth, imageHeight);
                ivProgress.setImageBitmap(resource);
                updateProgressClip();
                requestLayout();
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                ivProgress.setImageDrawable(placeholder);
            }
        };

        backgroundTarget = new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                if (sourceResId == null || sourceResId != drawableResId) {
                    return;
                }
                ivBackground.setImageBitmap(resource);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                ivBackground.setImageDrawable(placeholder);
            }
        };

        Glide.with(this)
                .asBitmap()
                .load(drawableResId)
                .into(progressTarget);

        Glide.with(this)
                .asBitmap()
                .load(drawableResId)
                .transform(new GrayScaleBitmapTransformation())
                .into(backgroundTarget);
    }

    public void setProgress(float progress) {
        if (Float.isNaN(progress)) {
            progress = 0f;
        }
        this.progress = Math.max(0f, Math.min(1f, progress));
        updateProgressClip();
    }

    public float getProgress() {
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
        if (sourceBitmap == null) {
            return;
        }

        int fullWidth = imageWidth;
        int height = imageHeight;
        int cropWidth = Math.round(fullWidth * progress);
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

    private void updateImageViewSize(int width, int height) {
        LayoutParams bgLp = (LayoutParams) ivBackground.getLayoutParams();
        bgLp.width = width;
        bgLp.height = height;
        ivBackground.setLayoutParams(bgLp);

        LayoutParams fgLp = (LayoutParams) ivProgress.getLayoutParams();
        fgLp.width = width;
        fgLp.height = height;
        ivProgress.setLayoutParams(fgLp);
    }

    private void clearCurrentRequests() {
        if (progressTarget != null) {
            Glide.with(this).clear(progressTarget);
            progressTarget = null;
        }
        if (backgroundTarget != null) {
            Glide.with(this).clear(backgroundTarget);
            backgroundTarget = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        clearCurrentRequests();
        super.onDetachedFromWindow();
    }
}
