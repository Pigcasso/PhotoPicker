package io.pigcasso.photopicker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class PhotoPickerActivity : AppCompatActivity(), PhotoPickerFragment.OnPhotoPickerListener {

    companion object {
        const val EXTRA_RESULT_SELECTION = "extra_result_selection"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_photo_picker)

        var fragment = supportFragmentManager.findFragmentById(R.id.contentFrame)
        if (fragment == null) {
            fragment = PhotoPickerFragment()
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