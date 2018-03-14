package io.pigcasso.photopicker.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.pigcasso.photopicker.PhotoPickerFragment

class PhotoPickerActivity : AppCompatActivity(), PhotoPickerFragment.OnPhotoPickerListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_photo_picker)

        var fragment = supportFragmentManager.findFragmentById(R.id.contentFrame)
        if (fragment == null) {
            fragment = PhotoPickerFragment()
            supportFragmentManager.beginTransaction().add(R.id.contentFrame, fragment).commit()
        }
    }

    override fun onPhotosSelect(photoPaths: List<String>) {
        supportActionBar?.title = getString(R.string.module_photo_picker_select_photo_count, photoPaths.size)
    }
}
