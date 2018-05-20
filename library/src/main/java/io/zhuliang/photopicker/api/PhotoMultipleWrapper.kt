package io.zhuliang.photopicker.api

import android.content.Context

/**
 * @author Zhu Liang
 */
class PhotoMultipleWrapper(private val mContext: Context) {


    fun upperLimit(): PhotoMultipleUpperLimitWrapper {
        return PhotoMultipleUpperLimitWrapper(mContext)
    }

    fun noUpperLimit(): PhotoMultipleNoUpperLimitWrapper {
        return PhotoMultipleNoUpperLimitWrapper(mContext)
    }
}