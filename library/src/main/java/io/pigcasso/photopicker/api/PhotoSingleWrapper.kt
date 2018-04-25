package io.pigcasso.photopicker.api

import android.content.Context
import io.pigcasso.photopicker.PhotoPickerActivity

/**
 * @author Zhu Liang
 */
class PhotoSingleWrapper(context: Context) : BasicWrapper<ArrayList<String>, String>(context) {

    private var allPhotosAlbum: Boolean = true
    private var preview: Boolean = true

    fun allPhotosAlbum(allPhotosAlbum: Boolean): PhotoSingleWrapper {
        this.allPhotosAlbum = allPhotosAlbum
        return this
    }

    fun preview(preview: Boolean): PhotoSingleWrapper {
        this.preview = preview
        return this
    }

    override fun start() {
        PhotoPickerActivity.requestCode = mRequestCode
        PhotoPickerActivity.result = mResult
        PhotoPickerActivity.cancel = mCancel
        PhotoPickerActivity.singleChoice(mContext, allPhotosAlbum, preview)
    }
}