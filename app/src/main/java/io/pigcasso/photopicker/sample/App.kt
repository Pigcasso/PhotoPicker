package io.pigcasso.photopicker.sample

import android.app.Application
import android.graphics.Color
import android.support.v4.content.ContextCompat
import io.pigcasso.photopicker.PhotoPicker
import io.pigcasso.photopicker.ThemeConfig

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
    }
}