package io.zhuliang.photopicker.sample

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.GridView
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import io.zhuliang.photopicker.*
import io.zhuliang.photopicker.api.Action


/**
 * @author Zhu Liang
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val PICK_PHOTOS_MULTIPLE = 0x123
        private const val PICK_PHOTOS_SINGLE = 0x124
    }

    private var choiceMode = -1
    private lateinit var result: Action<ArrayList<String>>
    private lateinit var cancel: Action<String>

    private lateinit var choiceModeRadioGroup: RadioGroup
    private lateinit var limitCountLl: View
    private lateinit var limitCountEdit: EditText
    private lateinit var allPhotosAlbumCheck: CheckBox
    private lateinit var previewCheck: CheckBox
    private lateinit var countableCheck: CheckBox
    private lateinit var selectableAllCheck: CheckBox
    private lateinit var gridView: GridView
    private lateinit var pathsAdapter: CommonAdapter<String>
    private lateinit var urisAdapter: CommonAdapter<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        result = object : Action<ArrayList<String>> {
            override fun onAction(requestCode: Int, result: ArrayList<String>) {
                gridView.adapter = pathsAdapter
                pathsAdapter.replaceData(result)
            }
        }

        cancel = object : Action<String> {
            override fun onAction(requestCode: Int, result: String) {

            }
        }

        choiceModeRadioGroup = findViewById(R.id.radio_group_main_choice_mode)
        limitCountLl = findViewById(R.id.ll_main_limit_count_group)
        limitCountEdit = findViewById(R.id.et_main_limit_count)
        allPhotosAlbumCheck = findViewById(R.id.checkbox_main_all_photos_album)
        previewCheck = findViewById(R.id.checkbox_main_preview)
        countableCheck = findViewById(R.id.checkbox_main_countable)
        selectableAllCheck = findViewById(R.id.checkbox_main_selectable_all)
        gridView = findViewById(R.id.gv_main_results)

        pathsAdapter = PathsAdapter(this)
        urisAdapter = UrisAdapter(this)

        val choiceMode = savedInstanceState?.getInt(EXTRA_CHOICE_MODE, choiceMode)
                ?: CHOICE_MODE_SINGLE
        setChoiceMode(choiceMode)

        choiceModeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.radio_button_main_choice_mode_single -> {
                    CHOICE_MODE_SINGLE
                }
                R.id.radio_button_main_choice_mode_multiple_upper_limit -> {
                    CHOICE_MODE_MULTIPLE_UPPER_LIMIT
                }
                R.id.radio_button_main_choice_mode_multiple_no_upper_limit -> {
                    CHOICE_MODE_MULTIPLE_NO_UPPER_LIMIT
                }
                else -> {
                    throw IllegalStateException("Invalid checked id: $checkedId")
                }
            }
            setChoiceMode(mode)
        }
    }

    private fun setChoiceMode(choiceMode: Int) {
        if (this.choiceMode != choiceMode) {
            this.choiceMode = choiceMode

            when (choiceMode) {
                CHOICE_MODE_SINGLE -> {
                    choiceModeRadioGroup.check(R.id.radio_button_main_choice_mode_single)
                    limitCountLl.visibility = View.GONE
                    countableCheck.visibility = View.GONE
                    selectableAllCheck.visibility = View.GONE
                }
                CHOICE_MODE_MULTIPLE_UPPER_LIMIT -> {
                    choiceModeRadioGroup.check(R.id.radio_button_main_choice_mode_multiple_upper_limit)
                    limitCountLl.visibility = View.VISIBLE
                    countableCheck.visibility = View.VISIBLE
                    selectableAllCheck.visibility = View.GONE
                }
                CHOICE_MODE_MULTIPLE_NO_UPPER_LIMIT -> {
                    choiceModeRadioGroup.check(R.id.radio_button_main_choice_mode_multiple_no_upper_limit)
                    limitCountLl.visibility = View.GONE
                    countableCheck.visibility = View.VISIBLE
                    selectableAllCheck.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showPhotoPicker() {
        when (choiceMode) {
            CHOICE_MODE_SINGLE -> {
                PhotoPicker
                        .image(this)
                        .singleChoice()
                        .allPhotosAlbum(allPhotosAlbumCheck.isChecked)
                        .preview(previewCheck.isChecked)
                        .onResult(result)
                        .onCancel(cancel)
                        .start()
            }
            CHOICE_MODE_MULTIPLE_UPPER_LIMIT -> {
                val limitCount = if (limitCountEdit.text.isNotEmpty()) {
                    limitCountEdit.text.toString().toInt()
                } else {
                    DEFAULT_LIMIT_COUNT
                }

                PhotoPicker
                        .image(this)
                        .multipleChoice()
                        .upperLimit()
                        .allPhotosAlbum(allPhotosAlbumCheck.isChecked)
                        .preview(previewCheck.isChecked)
                        .limitCount(limitCount)
                        .countable(countableCheck.isChecked)
                        .onResult(result)
                        .onCancel(cancel)
                        .start()
            }
            CHOICE_MODE_MULTIPLE_NO_UPPER_LIMIT -> {
                PhotoPicker
                        .image(this)
                        .multipleChoice()
                        .noUpperLimit()
                        .allPhotosAlbum(allPhotosAlbumCheck.isChecked)
                        .preview(previewCheck.isChecked)
                        .countable(countableCheck.isChecked)
                        .selectableAll(selectableAllCheck.isChecked)
                        .onResult(result)
                        .onCancel(cancel)
                        .start()
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onShowPhotoPicker(view: View) {
        showPhotoPicker()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onPickSingleImage(view: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_PHOTOS_SINGLE)
    }

    @Suppress("UNUSED_PARAMETER")
    fun onPickMultiImages(view: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, PICK_PHOTOS_MULTIPLE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (PICK_PHOTOS_MULTIPLE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                if (data != null) {
                    val uris = mutableListOf<Uri>()
                    val clipData = data.clipData
                    val uriData = data.data
                    if (clipData != null) {
                        val count = clipData.itemCount
                        for (i in 0 until count) {
                            val imageUri = clipData.getItemAt(i).uri
                            Log.d(TAG, "onActivityResult: requestCode $requestCode from data.clipData $imageUri")
                            uris.add(imageUri)
                        }
                    } else if (uriData != null) {
                        Log.d(TAG, "onActivityResult: requestCode $requestCode data.data $uriData")
                        uris.add(uriData)
                    }
                    gridView.adapter = urisAdapter
                    urisAdapter.replaceData(uris)
                } else {
                    Log.e(TAG, "data is null")
                }
            }
        } else if (PICK_PHOTOS_SINGLE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                if (data?.data != null) {
                    Log.d(TAG, "onActivityResult: requestCode $requestCode data.data ${data.data}")
                    gridView.adapter = urisAdapter
                    urisAdapter.replaceData(listOf(data.data!!))
                }
            }
        }
    }

    private inner class PathsAdapter(activity: Activity) : CommonAdapter<String>(R.layout.grid_item_result, ArrayList()) {
        private val itemSize: Int

        init {
            val dm = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(dm)
            val numColumns = activity.resources.getInteger(io.zhuliang.photopicker.R.integer.grid_num_columns)
            val horizontalSpacing = activity.resources.getDimensionPixelSize(io.zhuliang.photopicker.R.dimen.grid_horizontal_spacing)
            itemSize = (dm.widthPixels - (numColumns + 1) * horizontalSpacing) / numColumns
        }

        override fun bindView(viewHolder: ViewHolder, value: String, position: Int) {
            PhotoPicker.photoLoader.loadPhoto(viewHolder.findViewById(R.id.iv_main_photo_thumb)!!,
                    value, itemSize, itemSize)
        }
    }

    private inner class UrisAdapter(private val activity: Activity) : CommonAdapter<Uri>(R.layout.grid_item_result, ArrayList()) {
        private val itemSize: Int

        init {
            val dm = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(dm)
            val numColumns = activity.resources.getInteger(io.zhuliang.photopicker.R.integer.grid_num_columns)
            val horizontalSpacing = activity.resources.getDimensionPixelSize(io.zhuliang.photopicker.R.dimen.grid_horizontal_spacing)
            itemSize = (dm.widthPixels - (numColumns + 1) * horizontalSpacing) / numColumns
        }

        override fun bindView(viewHolder: ViewHolder, value: Uri, position: Int) {
            Glide.with(activity).load(value).into(viewHolder.findViewById(R.id.iv_main_photo_thumb)!!)
        }
    }
}