package io.zhuliang.photopicker.api

import android.content.Context

class PhotoChoice(private val context: Context) : Choice<PhotoMultipleWrapper, PhotoSingleWrapper> {

    override fun multipleChoice() = PhotoMultipleWrapper(context)

    override fun singleChoice() = PhotoSingleWrapper(context)
}