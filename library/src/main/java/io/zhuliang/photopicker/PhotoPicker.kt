package io.zhuliang.photopicker

import android.content.Context
import io.zhuliang.photopicker.api.PhotoChoice

/**
 * @author Zhu Liang
 */
class PhotoPicker {

    companion object {
        var photoLoader: PhotoLoader = DefaultPhotoLoader
        var themeConfig = ThemeConfig()

        fun image(context: Context): PhotoChoice {
            return PhotoChoice(context)
        }

    }
}