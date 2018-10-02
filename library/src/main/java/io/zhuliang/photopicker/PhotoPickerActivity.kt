package io.zhuliang.photopicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import io.zhuliang.photopicker.api.Action

class PhotoPickerActivity : AppCompatActivity(), PhotoPickerFragment.OnPhotoPickerListener {

    companion object {
        const val EXTRA_RESULT_SELECTION = "extra_result_selection"

        var result: Action<ArrayList<String>>? = null
        var requestCode: Int = -1
        var cancel: Action<String>? = null

        fun singleChoice(context: Context, allPhotosAlbum: Boolean, preview: Boolean): Intent {
            val intent = Intent(context, PhotoPickerActivity::class.java)
            intent.putExtra(EXTRA_ALL_PHOTOS_ALBUM, allPhotosAlbum)
            intent.putExtra(EXTRA_CHOICE_MODE, CHOICE_MODE_SINGLE)
            intent.putExtra(EXTRA_PREVIEW, preview)
            return intent
        }

        fun multiChoice(context: Context, allPhotosAlbum: Boolean, choiceMode: Int, limitCount: Int, countable: Boolean, preview: Boolean, selectableAll: Boolean): Intent {
            check(CHOICE_MODE_MULTIPLE_UPPER_LIMIT == choiceMode || CHOICE_MODE_MULTIPLE_NO_UPPER_LIMIT == choiceMode)
            val intent = Intent(context, PhotoPickerActivity::class.java)
            intent.putExtra(EXTRA_ALL_PHOTOS_ALBUM, allPhotosAlbum)
            intent.putExtra(EXTRA_CHOICE_MODE, choiceMode)
            intent.putExtra(EXTRA_LIMIT_COUNT, limitCount)
            intent.putExtra(EXTRA_COUNTABLE, countable)
            intent.putExtra(EXTRA_PREVIEW, preview)
            intent.putExtra(EXTRA_SELECTABLE_ALL, selectableAll)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_photo_picker)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(PhotoPicker.themeConfig.actionBarBackground))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = PhotoPicker.themeConfig.statusBarColor
        }

        checkNotNull(intent)

        var fragment = supportFragmentManager.findFragmentById(R.id.contentFrame)
        if (fragment == null) {
            fragment = PhotoPickerFragment.newInstance(
                    intent!!.getBooleanExtra(EXTRA_ALL_PHOTOS_ALBUM, true),
                    intent!!.getIntExtra(EXTRA_CHOICE_MODE, CHOICE_MODE_MULTIPLE_NO_UPPER_LIMIT),
                    intent!!.getIntExtra(EXTRA_LIMIT_COUNT, NO_LIMIT_COUNT),
                    intent!!.getBooleanExtra(EXTRA_COUNTABLE, false),
                    intent!!.getBooleanExtra(EXTRA_PREVIEW, true),
                    intent!!.getBooleanExtra(EXTRA_SELECTABLE_ALL, false))
            supportFragmentManager.beginTransaction().add(R.id.contentFrame, fragment).commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu ?: return super.onCreateOptionsMenu(menu)

        val menuItem = menu.add(R.id.group_menu_photo_picker_select_done, R.id.menu_photo_picker_select_done,
                Menu.NONE, R.string.module_photo_picker_select_done)
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item ?: return super.onOptionsItemSelected(item)

        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.menu_photo_picker_select_done -> {
                selectDone()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        cancel?.onAction(requestCode, "User canceled.")
        super.onBackPressed()
    }

    /**
     * 这个回调仅仅用于更新标题栏
     */
    override fun onPhotosSelect(photoPaths: ArrayList<String>) {
        supportActionBar?.title = getString(R.string.module_photo_picker_select_photo_count, photoPaths.size)
    }

    /**
     * 用户选好图片后，点击了完成按钮
     */
    private fun selectDone() {
        val fragment = supportFragmentManager.findFragmentById(R.id.contentFrame)
        if (fragment != null && fragment.isAdded && fragment is PhotoPickerFragment) {
            val photoPaths = fragment.getAllCheckedPhotos()
            if (photoPaths.isEmpty()) {
                Toast.makeText(this, R.string.module_photo_picker_select_no_images, Toast.LENGTH_SHORT).show()
                return
            }
            if (result != null) {
                result!!.onAction(requestCode, photoPaths)
                setResult(Activity.RESULT_OK)
            } else {
                val data = Intent()
                data.putStringArrayListExtra(EXTRA_RESULT_SELECTION, photoPaths)
                setResult(Activity.RESULT_OK, data)
            }
            finish()
        }
    }

    override fun finish() {
        requestCode = -1
        result = null
        cancel = null

        super.finish()
    }
}