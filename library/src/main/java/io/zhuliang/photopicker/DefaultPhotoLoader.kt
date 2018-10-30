package io.zhuliang.photopicker

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import androidx.collection.LruCache
import android.widget.ImageView
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @author Zhu Liang
 */
object DefaultPhotoLoader : PhotoLoader {

    private var mPlaceHolderDrawable: Drawable = ColorDrawable(Color.parseColor("#FF2B2B2B"))
        set(value) {
            field = value
        }
    private var mErrorDrawable: Drawable = ColorDrawable(Color.parseColor("#FF2B2B2B"))
        set(value) {
            field = value
        }
    private val mLruCache: androidx.collection.LruCache<String, Bitmap>
    private val mUIHandler: Handler
    private val mExecutorService: ExecutorService

    init {
        val maxMemory = Runtime.getRuntime().maxMemory() / 4
        mLruCache = object : androidx.collection.LruCache<String, Bitmap>(maxMemory.toInt()) {
            override fun sizeOf(key: String, value: Bitmap): Int {
                return value.rowBytes * value.height
            }
        }
        mUIHandler = Handler(Looper.getMainLooper())
        mExecutorService = Executors.newFixedThreadPool(6)
    }

    override fun loadPhoto(imageView: ImageView, imagePath: String, viewWidth: Int, viewHeight: Int) {
        val imageInfo = ImageInfo(imagePath, viewWidth, viewHeight)
        val bitmap = getBitmapFromCache(imageInfo)
        imageView.tag = imageInfo.toString()
        if (bitmap == null) {
            imageView.setImageDrawable(mPlaceHolderDrawable)
            mExecutorService.execute(LoadImageTask(imageView, imageInfo))
        } else {
            mUIHandler.post(BitmapHolder(imageView, bitmap, imageInfo))
        }
    }

    private fun getBitmapFromCache(imageInfo: ImageInfo): Bitmap? {
        synchronized(mLruCache, {
            return mLruCache.get(imageInfo.toString())
        })
    }

    private fun addBitmapToCache(bitmap: Bitmap?, imageInfo: ImageInfo) {
        if (getBitmapFromCache(imageInfo) == null && bitmap != null) {
            synchronized(mLruCache, {
                mLruCache.put(imageInfo.toString(), bitmap)
            })
        }
    }

    private fun loadBitmapFromPath(pathName: String, width: Int, height: Int): Bitmap? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(pathName, options)

        options.inSampleSize = calculateInSampleSize(options, width, height)
        options.inJustDecodeBounds = false

        return try {
            BitmapFactory.decodeFile(pathName, options)
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val width = options.outWidth
        val height = options.outHeight
        var inSampleSize = 1

        if (width > reqWidth || height > reqHeight) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfWidth / inSampleSize > reqWidth
                    && halfHeight / inSampleSize > reqHeight) {
                inSampleSize *= 2
            }

            var totalPixels = width * height / inSampleSize
            val totalReqPixelsCap = reqWidth * reqHeight * 2

            while (totalPixels > totalReqPixelsCap) {
                inSampleSize *= 2
                totalPixels /= 2
            }
        }

        return inSampleSize
    }


    private class LoadImageTask(val imageView: ImageView, val imageInfo: ImageInfo) : Runnable {
        override fun run() {
            val bitmap = loadBitmapFromPath(imageInfo.imagePath, imageInfo.viewWidth, imageInfo.viewHeight)
            addBitmapToCache(bitmap, imageInfo)

            mUIHandler.post(BitmapHolder(imageView, bitmap, imageInfo))
        }
    }

    private class ImageInfo constructor(val imagePath: String, val viewWidth: Int, val viewHeight: Int) {
        override fun toString(): String {
            return "$imagePath-$viewWidth-$viewHeight"
        }
    }

    private class BitmapHolder(val imageView: ImageView, val bitmap: Bitmap?, val imageInfo: ImageInfo) : Runnable {

        override fun run() {
            if (imageView.tag == imageInfo.toString()) {
                if (bitmap == null) {
                    imageView.setImageDrawable(mErrorDrawable)
                } else {
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }
}