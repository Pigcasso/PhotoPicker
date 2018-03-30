package io.pigcasso.photopicker.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import io.pigcasso.photopicker.CHOICE_MODE_MULTIPLE_NO_UPPER_LIMIT
import io.pigcasso.photopicker.CHOICE_MODE_MULTIPLE_UPPER_LIMIT
import io.pigcasso.photopicker.NO_LIMIT_COUNT
import io.pigcasso.photopicker.PhotoPickerActivity
import io.pigcasso.photopicker.PhotoPickerActivity.Companion.EXTRA_RESULT_SELECTION

/**
 * @author Zhu Liang
 */
class MainActivity : AppCompatActivity() {

    private lateinit var mAllPhotosAlbumSwitch: Switch
    private lateinit var mMultiChoiceNoUpperLimitSwitch: Switch
    private lateinit var mCountableSwitch: Switch
    private lateinit var mLimitCountEt: EditText
    private lateinit var mPreviewSwitch: Switch
    private lateinit var mSelectableAllSwitch: Switch

    companion object {
        private const val RC_PHOTO_PICKER = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        mAllPhotosAlbumSwitch = findViewById(R.id.switch_sample_all_photos_album)
        mMultiChoiceNoUpperLimitSwitch = findViewById(R.id.switch_sample_multi_choice_no_upper_limit)
        mCountableSwitch = findViewById(R.id.switch_sample_countable)
        mLimitCountEt = findViewById(R.id.et_sample_limit_count)
        mPreviewSwitch = findViewById(R.id.switch_sample_preview)
        mSelectableAllSwitch = findViewById(R.id.switch_sample_select_all)

        mMultiChoiceNoUpperLimitSwitch.setOnCheckedChangeListener({ _, isChecked ->
            mLimitCountEt.isEnabled = !isChecked
            mSelectableAllSwitch.isChecked = isChecked
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == RC_PHOTO_PICKER) {
            val result = data!!.getStringArrayListExtra(EXTRA_RESULT_SELECTION)
            Toast.makeText(this, "Selected photos: ${result.size}", Toast.LENGTH_SHORT).show()
        }
    }

    fun onSingleChoice(view: View) {
        val allPhotosAlbum = mAllPhotosAlbumSwitch.isChecked

        val intent = PhotoPickerActivity.singleChoice(this, allPhotosAlbum, mPreviewSwitch.isChecked)
        startActivityForResult(intent, RC_PHOTO_PICKER)
    }

    fun onMultiChoice(view: View) {

        val allPhotosAlbum = mAllPhotosAlbumSwitch.isChecked
        val choiceMode: Int
        val limitCount: Int
        val allowSelectAll: Boolean
        if (mMultiChoiceNoUpperLimitSwitch.isChecked) {
            choiceMode = CHOICE_MODE_MULTIPLE_NO_UPPER_LIMIT
            limitCount = NO_LIMIT_COUNT
            allowSelectAll = mSelectableAllSwitch.isChecked
        } else {
            limitCount = if (mLimitCountEt.text.isEmpty()) {
                9
            } else {
                mLimitCountEt.text.toString().toInt()
            }
            choiceMode = CHOICE_MODE_MULTIPLE_UPPER_LIMIT
            allowSelectAll = false
        }

        val intent = PhotoPickerActivity.multiChoice(this, allPhotosAlbum, choiceMode, limitCount, mCountableSwitch.isChecked, mPreviewSwitch.isChecked, allowSelectAll)
        startActivityForResult(intent, RC_PHOTO_PICKER)
    }
}