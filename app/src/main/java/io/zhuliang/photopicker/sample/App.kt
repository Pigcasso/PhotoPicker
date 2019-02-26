package io.zhuliang.photopicker.sample

import android.app.Application
import android.graphics.Color
import androidx.core.content.ContextCompat
import io.zhuliang.photopicker.PhotoPicker
import io.zhuliang.photopicker.ThemeConfig

/**
 * @author Zhu Liang
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        PhotoPicker.authority = "${BuildConfig.APPLICATION_ID}.provider"
        PhotoPicker.photoLoader = GlidePhotoLoader()
        PhotoPicker.themeConfig = ThemeConfig()
                .radioCheckedColor(ContextCompat.getColor(this@App, R.color.colorAccent))
                .bottomBarBackgroundColor(ContextCompat.getColor(this@App, R.color.colorPrimary))
                .bottomBarTextColor(Color.WHITE)
                .arrowDropColor(Color.WHITE)
                .checkboxColor(ContextCompat.getColor(this@App, R.color.colorAccent))
                .checkboxOutlineColor(ContextCompat.getColor(this@App, R.color.colorAccent))
                .orderedCheckedBackground(R.drawable.ic_app_badge_checked_24dp)
                .orderedUncheckedBackground(R.drawable.ic_app_badge_unchecked_24dp)
                .actionBarBackground(ContextCompat.getColor(this@App, R.color.colorPrimary))
                .statusBarColor(ContextCompat.getColor(this@App, R.color.colorPrimaryDark))
    }
}