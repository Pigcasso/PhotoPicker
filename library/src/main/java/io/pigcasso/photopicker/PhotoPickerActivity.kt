package io.pigcasso.photopicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class PhotoPickerActivity : AppCompatActivity(), PhotoPickerFragment.OnPhotoPickerListener {

    companion object {
        const val EXTRA_RESULT_SELECTION = "extra_result_selection"

        fun singleChoice(context: Context, allPhotosAlbum: Boolean): Intent {
            val intent = Intent(context, PhotoPickerActivity::class.java)
            intent.putExtra(EXTRA_ALL_PHOTOS_ALBUM, allPhotosAlbum)
            intent.putExtra(EXTRA_CHOICE_MODE, CHOICE_MODE_SINGLE)
            return intent
        }

        fun multiChoice(context: Context, allPhotosAlbum: Boolean, choiceMode: Int, limitCount: Int, countable: Boolean): Intent {
            check(CHOICE_MODE_MULTIPLE_UPPER_LIMIT == choiceMode || CHOICE_MODE_MULTIPLE_NO_UPPER_LIMIT == choiceMode)
            val intent = Intent(context, PhotoPickerActivity::class.java)
            intent.putExtra(EXTRA_ALL_PHOTOS_ALBUM, allPhotosAlbum)
            intent.putExtra(EXTRA_CHOICE_MODE, choiceMode)
            intent.putExtra(EXTRA_LIMIT_COUNT, limitCount)
            intent.putExtra(EXTRA_COUNTABLE, countable)
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
                    intent!!.getBooleanExtra(EXTRA_COUNTABLE, false))
            supportFragmentManager.beginTransaction().add(R.id.contentFrame, fragment).commit()
        }
    }

    /**
     * 这个回调仅仅用于更新标题栏
     */
    override fun onPhotosSelect(photoPaths: List<String>) {
        supportActionBar?.title = getString(R.string.module_photo_picker_select_photo_count, photoPaths.size)
    }

    override fun onSelectedResult(photoPaths: ArrayList<String>) {
        val data = Intent()
        data.putStringArrayListExtra(EXTRA_RESULT_SELECTION, photoPaths)
        setResult(Activity.RESULT_OK, data)
        finish()
    }
}