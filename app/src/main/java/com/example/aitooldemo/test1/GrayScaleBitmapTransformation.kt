package com.example.aitooldemo.test1

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation

class GrayScaleBitmapTransformation : BitmapTransformation() {
    protected override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val config = if (toTransform.config != null) toTransform.config else Bitmap.Config.ARGB_8888
        val result: Bitmap = pool.get(toTransform.width, toTransform.height, config)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val matrix: ColorMatrix = ColorMatrix()
        matrix.setSaturation(0f)
        paint.setColorFilter(ColorMatrixColorFilter(matrix))
        canvas.drawBitmap(toTransform, 0f, 0f, paint)
        return result
    }

    override fun equals(o: Any?): Boolean {
        return o is GrayScaleBitmapTransformation
    }

    override fun hashCode(): Int {
        return ID.hashCode()
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
    }

    companion object {
        private const val ID = "com.example.aitooldemo.test1.GrayScaleBitmapTransformation.v1"
        private val ID_BYTES = ID.toByteArray(StandardCharsets.UTF_8)
    }
}
