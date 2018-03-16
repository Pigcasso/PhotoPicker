package io.pigcasso.photopicker.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import io.pigcasso.photopicker.PhotoPickerActivity
import io.pigcasso.photopicker.PhotoPickerActivity.Companion.EXTRA_RESULT_SELECTION

/**
 * @author Zhu Liang
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val RC_PHOTO_PICKER = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == RC_PHOTO_PICKER) {
            val result = data!!.getStringArrayListExtra(EXTRA_RESULT_SELECTION)
            Toast.makeText(this, "Selected photos: ${result.size}", Toast.LENGTH_SHORT).show()
        }
    }

    fun onPhotoPicker(view: View) {
        val intent = Intent(this, PhotoPickerActivity::class.java)
        startActivityForResult(intent, RC_PHOTO_PICKER)
    }
}