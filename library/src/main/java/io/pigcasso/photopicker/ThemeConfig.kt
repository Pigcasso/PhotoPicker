package io.pigcasso.photopicker

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat

/**
 * @author Zhu Liang
 */
class ThemeConfig {
    var bottomBarBackgroundColor: Int = Color.BLUE
    var bottomBarTextColor: Int = Color.WHITE
    var checkboxOutlineColor: Int = Color.LTGRAY
    var checkboxColor: Int = Color.CYAN
    var orderedUncheckedBackground: Int = R.drawable.ic_badge_unchecked_24dp
    var orderedCheckedBackground: Int = R.drawable.ic_badge_checked_24dp
    var radioCheckedColor: Int = Color.YELLOW
    var arrowDropColor: Int = Color.GREEN

    companion object {
        fun tint(resources: Context, drawableId: Int, tint: Int): Drawable {
            val wrapper = DrawableCompat.wrap(ContextCompat.getDrawable(resources, drawableId)!!)
            DrawableCompat.setTint(wrapper, tint)
            return wrapper
        }
    }
}