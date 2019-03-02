package io.zhuliang.photopicker.sample

import android.app.Application
import android.graphics.Color
import androidx.core.content.ContextCompat
import io.zhuliang.photopicker.PhotoPicker
import io.zhuliang.photopicker.ThemeConfig
import timber.log.Timber

/**
 * @author Zhu Liang
 */
class SampleApp : Application() {

    companion object {
        private const val TAG = "SampleApp"
    }

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
        Timber.tag(TAG)
        Timber.d("onCreate: ")

        // 应用内使用 PhotoPicker 时，需要在 Application 里初始化
        initPhotoPicker()
    }

    private fun initPhotoPicker() {
        PhotoPicker.themeConfig = ThemeConfig()
                .radioCheckedColor(ContextCompat.getColor(this, R.color.colorAccent))
                .bottomBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .bottomBarTextColor(Color.WHITE)
                .arrowDropColor(Color.WHITE)
                .checkboxColor(ContextCompat.getColor(this, R.color.colorAccent))
                .checkboxOutlineColor(ContextCompat.getColor(this, R.color.colorAccent))
                .orderedCheckedBackground(R.drawable.ic_app_badge_checked_24dp)
                .orderedUncheckedBackground(R.drawable.ic_app_badge_unchecked_24dp)
                .actionBarBackground(ContextCompat.getColor(this, R.color.colorPrimary))
                .statusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        PhotoPicker.authority = "${BuildConfig.APPLICATION_ID}.provider"
        PhotoPicker.photoLoader = GlidePhotoLoader()
    }
}