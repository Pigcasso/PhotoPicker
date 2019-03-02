package io.zhuliang.photopicker

import android.app.Activity
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import io.zhuliang.photopicker.api.Action
import java.io.File

class PhotoPickerActivity : AppCompatActivity(), PhotoPickerFragment.OnPhotoPickerListener {

    companion object {
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
            when (intent.action) {
                null -> {
                    fragment = PhotoPickerFragment.newInstance(
                            intent!!.getBooleanExtra(EXTRA_ALL_PHOTOS_ALBUM, DEFAULT_ALL_PHOTOS_ALBUM),
                            intent!!.getIntExtra(EXTRA_CHOICE_MODE, CHOICE_MODE_MULTIPLE_NO_UPPER_LIMIT),
                            intent!!.getIntExtra(EXTRA_LIMIT_COUNT, NO_LIMIT_COUNT),
                            intent!!.getBooleanExtra(EXTRA_COUNTABLE, DEFAULT_COUNTABLE),
                            intent!!.getBooleanExtra(EXTRA_PREVIEW, DEFAULT_PREVIEW),
                            intent!!.getBooleanExtra(EXTRA_SELECTABLE_ALL, DEFAULT_SELECTABLE_ALL))
                }
                Intent.ACTION_GET_CONTENT, Intent.ACTION_PICK -> {
                    val choiceMode: Int = if (intent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)) {
                        CHOICE_MODE_MULTIPLE_NO_UPPER_LIMIT
                    } else {
                        CHOICE_MODE_SINGLE
                    }
                    fragment = PhotoPickerFragment.newInstance(
                            intent!!.getBooleanExtra(EXTRA_ALL_PHOTOS_ALBUM, DEFAULT_ALL_PHOTOS_ALBUM),
                            intent!!.getIntExtra(EXTRA_CHOICE_MODE, choiceMode),
                            intent!!.getIntExtra(EXTRA_LIMIT_COUNT, NO_LIMIT_COUNT),
                            intent!!.getBooleanExtra(EXTRA_COUNTABLE, DEFAULT_COUNTABLE),
                            intent!!.getBooleanExtra(EXTRA_PREVIEW, DEFAULT_PREVIEW),
                            intent!!.getBooleanExtra(EXTRA_SELECTABLE_ALL, DEFAULT_SELECTABLE_ALL))
                }
                else -> throw IllegalStateException("Invalid action ${intent.action}")
            }
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
        if (cancel == null) {
            setResult(Activity.RESULT_CANCELED)
        } else {
            cancel?.onAction(requestCode, "User canceled.")
        }
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
                /*val data = Intent()
                data.putStringArrayListExtra(EXTRA_RESULT_SELECTION, photoPaths)
                setResult(Activity.RESULT_OK, data)*/
                // 获取一个文件
                if (photoPaths.size == 1) {
                    val data = Intent()
                    data.data = getFilePublicUri(File(photoPaths[0]), PhotoPicker.authority!!)
                    setResult(Activity.RESULT_OK, data)
                } else {
                    val data = Intent()
                    data.clipData = createClipData("Pick photos", photoPaths, PhotoPicker.authority!!)
                    setResult(Activity.RESULT_OK, data)
                }
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

    private fun createClipData(label: String, paths: List<String>, authority: String): ClipData {
        check(paths.size > 1)
        val clipData = ClipData(ClipDescription(label, arrayOf(ClipDescription.MIMETYPE_TEXT_URILIST)), ClipData.Item(getFilePublicUri(File(paths[0]), authority)))
        for (i in 1 until paths.size) {
            clipData.addItem(ClipData.Item(getFilePublicUri(File(paths[i]), authority)))
        }
        return clipData
    }

    /**
     * 参考果仁相册
     */
    private fun getFilePublicUri(file: File, authority: String): Uri {
        // for images/videos/gifs try getting a media content uri first, like content://media/external/images/media/438
        // if media content uri is null, get our custom uri like content://com.simplemobiletools.gallery.provider/external_files/emulated/0/DCIM/IMG_20171104_233915.jpg
        var uri = getMediaContent(file.absolutePath, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (uri == null) {
            uri = FileProvider.getUriForFile(this, authority, file)
        }
        return uri!!
    }

    private fun getMediaContent(path: String, uri: Uri): Uri? {
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection = MediaStore.Images.Media.DATA + "= ?"
        val selectionArgs = arrayOf(path)
        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
            if (cursor?.moveToFirst() == true) {
                val id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID)).toString()
                return Uri.withAppendedPath(uri, id)
            }
        } catch (e: Exception) {
        } finally {
            cursor?.close()
        }
        return null
    }
}