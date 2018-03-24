package io.pigcasso.photopicker.sample

import android.app.Application
import io.pigcasso.photopicker.PhotoPicker

/**
 * @author Zhu Liang
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        PhotoPicker.photoLoader = GlidePhotoLoader()
    }
}