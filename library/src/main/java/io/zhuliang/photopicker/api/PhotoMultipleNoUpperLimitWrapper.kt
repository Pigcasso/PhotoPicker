package io.zhuliang.photopicker.api

import android.content.Context
import io.zhuliang.photopicker.*

/**
 * @author Zhu Liang
 */
class PhotoMultipleNoUpperLimitWrapper(context: Context) : BasicWrapper<ArrayList<String>, String>(context) {

    private var allPhotosAlbum = DEFAULT_ALL_PHOTOS_ALBUM
    private var preview = DEFAULT_PREVIEW
    private var selectableAll = DEFAULT_SELECTABLE_ALL
    private var countable = DEFAULT_COUNTABLE

    fun allPhotosAlbum(allPhotosAlbum: Boolean): PhotoMultipleNoUpperLimitWrapper {
        this.allPhotosAlbum = allPhotosAlbum
        return this
    }

    fun preview(preview: Boolean): PhotoMultipleNoUpperLimitWrapper {
        this.preview = preview
        return this
    }

    fun selectableAll(selectableAll: Boolean): PhotoMultipleNoUpperLimitWrapper {
        this.selectableAll = selectableAll
        return this
    }

    fun countable(countable: Boolean): PhotoMultipleNoUpperLimitWrapper {
        this.countable = countable
        return this
    }

    override fun start() {
        PhotoPickerActivity.result = mResult
        PhotoPickerActivity.cancel = mCancel
        PhotoPickerActivity.requestCode = mRequestCode

        val intent = PhotoPickerActivity.multiChoice(mContext, allPhotosAlbum, CHOICE_MODE_MULTIPLE_NO_UPPER_LIMIT,
                NO_LIMIT_COUNT, countable, preview, selectableAll)
        mContext.startActivity(intent)
    }
}