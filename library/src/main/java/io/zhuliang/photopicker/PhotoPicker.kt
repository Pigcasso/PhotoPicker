package io.zhuliang.photopicker

import android.content.Context
import io.zhuliang.photopicker.api.PhotoChoice

/**
 * @author Zhu Liang
 */
class PhotoPicker {

    companion object {
        /**
         * 不再提供默认相片加载器
         */
        lateinit var photoLoader: PhotoLoader
        var themeConfig = ThemeConfig()
        /**
         * 用于 [androidx.core.content.FileProvider.getUriForFile]
         */
        var authority: String? = null

        fun image(context: Context): PhotoChoice {
            return PhotoChoice(context)
        }

    }
}