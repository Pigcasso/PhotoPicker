package io.zhuliang.photopicker.sample

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.GridView
import android.widget.RadioGroup
import io.zhuliang.photopicker.*
import io.zhuliang.photopicker.api.Action

/**
 * @author Zhu Liang
 */
class MainActivity : AppCompatActivity() {

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
    private lateinit var adapter: CommonAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        result = object : Action<ArrayList<String>> {
            override fun onAction(requestCode: Int, result: ArrayList<String>) {
                adapter.replaceData(result)
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
        val resultsGv = findViewById<GridView>(R.id.gv_main_results)

        adapter = ResultsAdapter(this)
        resultsGv.adapter = adapter

        val choiceMode = savedInstanceState?.getInt(EXTRA_CHOICE_MODE, choiceMode)
                ?: CHOICE_MODE_SINGLE
        setChoiceMode(choiceMode)

        choiceModeRadioGroup.setOnCheckedChangeListener({ _, checkedId ->
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
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item == null) {
            return super.onOptionsItemSelected(item)
        }
        if (item.itemId == R.id.menu_choice_done) {
            start()
            return true
        }
        return super.onOptionsItemSelected(item)
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

    private fun start() {
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

    private inner class ResultsAdapter(activity: Activity) : CommonAdapter<String>(R.layout.grid_item_result, ArrayList()) {
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
}