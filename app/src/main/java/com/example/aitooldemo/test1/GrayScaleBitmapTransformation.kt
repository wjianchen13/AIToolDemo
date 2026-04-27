package com.example.aitooldemo.test1;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class GrayScaleBitmapTransformation extends BitmapTransformation {

    private static final String ID = "com.example.aitooldemo.test1.GrayScaleBitmapTransformation.v1";
    private static final byte[] ID_BYTES = ID.getBytes(StandardCharsets.UTF_8);

    @Override
    protected Bitmap transform(
            @NonNull BitmapPool pool,
            @NonNull Bitmap toTransform,
            int outWidth,
            int outHeight
    ) {
        Bitmap.Config config = toTransform.getConfig() != null ? toTransform.getConfig() : Bitmap.Config.ARGB_8888;
        Bitmap result = pool.get(toTransform.getWidth(), toTransform.getHeight(), config);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0f);
        paint.setColorFilter(new ColorMatrixColorFilter(matrix));
        canvas.drawBitmap(toTransform, 0f, 0f, paint);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof GrayScaleBitmapTransformation;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }
}
