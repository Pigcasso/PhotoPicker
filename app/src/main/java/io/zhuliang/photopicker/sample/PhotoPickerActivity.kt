package io.zhuliang.photopicker.sample

import android.graphics.Color
import android.os.Bundle
import androidx.core.content.ContextCompat
import io.zhuliang.photopicker.PhotoPicker
import io.zhuliang.photopicker.ThemeConfig

/**
 * <pre>
 *     author : Julian
 *     time   : 2019/03/02
 *     desc   :
 *     version: 1.0
 * </pre>
 */

class PhotoPickerActivity : io.zhuliang.photopicker.PhotoPickerActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 将主题色设置的时机推迟到 onCreate
        PhotoPicker.themeConfig = ThemeConfig()
                .radioCheckedColor(ContextCompat.getColor(this@PhotoPickerActivity, R.color.colorAccent))
                .bottomBarBackgroundColor(ContextCompat.getColor(this@PhotoPickerActivity, R.color.colorPrimary))
                .bottomBarTextColor(Color.WHITE)
                .arrowDropColor(Color.WHITE)
                .checkboxColor(ContextCompat.getColor(this@PhotoPickerActivity, R.color.colorAccent))
                .checkboxOutlineColor(ContextCompat.getColor(this@PhotoPickerActivity, R.color.colorAccent))
                .orderedCheckedBackground(R.drawable.ic_app_badge_checked_24dp)
                .orderedUncheckedBackground(R.drawable.ic_app_badge_unchecked_24dp)
                .actionBarBackground(ContextCompat.getColor(this@PhotoPickerActivity, R.color.colorPrimary))
                .statusBarColor(ContextCompat.getColor(this@PhotoPickerActivity, R.color.colorPrimaryDark))
        super.onCreate(savedInstanceState)
    }
}