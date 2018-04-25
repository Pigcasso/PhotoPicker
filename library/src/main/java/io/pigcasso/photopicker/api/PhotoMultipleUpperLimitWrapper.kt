package io.pigcasso.photopicker.api

import android.content.Context
import android.support.annotation.IntRange
import io.pigcasso.photopicker.*

/**
 * @author Zhu Liang
 */
class PhotoMultipleUpperLimitWrapper(context: Context) : BasicWrapper<ArrayList<String>, String>(context) {

    private var allPhotosAlbum = DEFAULT_ALL_PHOTOS_ALBUM
    private var preview = DEFAULT_PREVIEW
    private var limitCount = DEFAULT_LIMIT_COUNT
    private var countable = DEFAULT_COUNTABLE

    fun allPhotosAlbum(allPhotosAlbum: Boolean): PhotoMultipleUpperLimitWrapper {
        this.allPhotosAlbum = allPhotosAlbum
        return this
    }

    fun preview(preview: Boolean): PhotoMultipleUpperLimitWrapper {
        this.preview = preview
        return this
    }

    fun limitCount(@IntRange(from = 1) limitCount: Int): PhotoMultipleUpperLimitWrapper {
        this.limitCount = limitCount
        return this
    }

    fun countable(countable: Boolean): PhotoMultipleUpperLimitWrapper {
        this.countable = countable
        return this
    }

    override fun start() {
        PhotoPickerActivity.result = mResult
        PhotoPickerActivity.cancel = mCancel
        PhotoPickerActivity.requestCode = mRequestCode

        val intent = PhotoPickerActivity.multiChoice(mContext, allPhotosAlbum, CHOICE_MODE_MULTIPLE_UPPER_LIMIT,
                limitCount, countable, preview, false)
        mContext.startActivity(intent)
    }
}