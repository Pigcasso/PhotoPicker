package io.zhuliang.photopicker.sample

import android.app.Application
import io.zhuliang.photopicker.PhotoPicker
import timber.log.Timber

/**
 * @author Zhu Liang
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        PhotoPicker.authority = "${BuildConfig.APPLICATION_ID}.provider"
        PhotoPicker.photoLoader = GlidePhotoLoader()
    }
}