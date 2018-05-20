package io.zhuliang.photopicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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
     * 点击完成时回调
     */
    override fun onSelectedResult(photoPaths: ArrayList<String>) {
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

    override fun finish() {
        requestCode = -1
        result = null
        cancel = null

        super.finish()
    }
}