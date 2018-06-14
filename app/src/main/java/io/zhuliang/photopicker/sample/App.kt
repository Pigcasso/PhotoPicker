package io.zhuliang.photopicker.sample

import android.app.Application
import android.graphics.Color
import android.support.v4.content.ContextCompat
import io.zhuliang.photopicker.PhotoPicker
import io.zhuliang.photopicker.ThemeConfig

/**
 * @author Zhu Liang
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        PhotoPicker.photoLoader = GlidePhotoLoader()
        PhotoPicker.themeConfig = ThemeConfig()
                .radioCheckedColor(Color.RED)
                .bottomBarBackgroundColor(ContextCompat.getColor(this@App, R.color.colorPrimary))
                .bottomBarTextColor(Color.MAGENTA)
                .arrowDropColor(Color.CYAN)
                .checkboxColor(ContextCompat.getColor(this@App, R.color.colorAccent))
                .checkboxOutlineColor(ContextCompat.getColor(this@App, R.color.colorAccent))
                .orderedCheckedBackground(R.drawable.ic_app_badge_checked_24dp)
                .orderedUncheckedBackground(R.drawable.ic_app_badge_unchecked_24dp)
    }
}