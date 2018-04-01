package io.pigcasso.photopicker.sample

import android.app.Application
import android.support.v4.content.ContextCompat
import io.pigcasso.photopicker.PhotoPicker
import io.pigcasso.photopicker.ThemeConfig

/**
 * @author Zhu Liang
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // PhotoPicker.photoLoader = GlidePhotoLoader()
        PhotoPicker.themeConfig = ThemeConfig().apply {
            bottomBarBackgroundColor = ContextCompat.getColor(this@App, R.color.colorPrimary)
            checkboxOutlineColor = ContextCompat.getColor(this@App, R.color.colorAccent)
            checkboxColor = ContextCompat.getColor(this@App, R.color.colorAccent)
        }
    }
}