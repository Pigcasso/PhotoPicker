package io.zhuliang.photopicker

import android.widget.ImageView

/**
 * @author Zhu Liang
 */
interface PhotoLoader {

    fun loadPhoto(imageView: ImageView, imagePath: String, viewWidth: Int, viewHeight: Int)
}