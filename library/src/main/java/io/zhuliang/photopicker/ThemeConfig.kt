package io.zhuliang.photopicker

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat

/**
 * @author Zhu Liang
 */
class ThemeConfig {
    internal var bottomBarBackgroundColor: Int = Color.BLUE
    internal var bottomBarTextColor: Int = Color.WHITE
    internal var checkboxOutlineColor: Int = Color.LTGRAY
    internal var checkboxColor: Int = Color.CYAN
    internal var orderedUncheckedBackground: Int = R.drawable.ic_badge_unchecked_24dp
    internal var orderedCheckedBackground: Int = R.drawable.ic_badge_checked_24dp
    internal var radioCheckedColor: Int = Color.YELLOW
    internal var arrowDropColor: Int = Color.GREEN
    internal var actionBarBackground = Color.RED
    internal var statusBarColor = Color.MAGENTA

    fun bottomBarBackgroundColor(bottomBarBackgroundColor: Int): ThemeConfig {
        this.bottomBarBackgroundColor = bottomBarBackgroundColor
        return this
    }

    fun bottomBarTextColor(bottomBarTextColor: Int): ThemeConfig {
        this.bottomBarTextColor = bottomBarTextColor
        return this
    }

    fun checkboxOutlineColor(checkboxOutlineColor: Int): ThemeConfig {
        this.checkboxOutlineColor = checkboxOutlineColor
        return this
    }

    fun checkboxColor(checkboxColor: Int): ThemeConfig {
        this.checkboxColor = checkboxColor
        return this
    }

    fun orderedUncheckedBackground(orderedUncheckedBackground: Int): ThemeConfig {
        this.orderedUncheckedBackground = orderedUncheckedBackground
        return this
    }

    fun orderedCheckedBackground(orderedCheckedBackground: Int): ThemeConfig {
        this.orderedCheckedBackground = orderedCheckedBackground
        return this
    }

    fun radioCheckedColor(radioCheckedColor: Int): ThemeConfig {
        this.radioCheckedColor = radioCheckedColor
        return this
    }

    fun arrowDropColor(arrowDropColor: Int): ThemeConfig {
        this.arrowDropColor = arrowDropColor
        return this
    }

    fun actionBarBackground(actionBarBackground: Int): ThemeConfig {
        this.actionBarBackground = actionBarBackground
        return this
    }

    fun statusBarColor(statusBarColor: Int): ThemeConfig {
        this.statusBarColor = statusBarColor
        return this
    }

    companion object {
        fun tint(resources: Context, drawableId: Int, tint: Int): Drawable {
            val wrapper = DrawableCompat.wrap(ContextCompat.getDrawable(resources, drawableId)!!)
            DrawableCompat.setTint(wrapper, tint)
            return wrapper
        }
    }
}