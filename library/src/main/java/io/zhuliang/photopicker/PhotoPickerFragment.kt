package io.zhuliang.photopicker

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.app.ActivityCompat
import java.lang.ref.WeakReference

/**
 * @author Zhu Liang
 */
class PhotoPickerFragment : androidx.fragment.app.Fragment() {

    private var mSelectedAlbum: Album? = null
    private lateinit var mAlbumsAdapter: AlbumsAdapter
    private var mPhotosAdapter: CommonAdapter<Photo>? = null
    private lateinit var mCheckedPhotos: ArrayList<String>
    private var mOnPhotoPickerListener: OnPhotoPickerListener? = null

    private var mChoiceMode: Int = CHOICE_MODE_MULTIPLE_NO_UPPER_LIMIT
    /**
     * 照片可选的上限数，如果等于[-1]表示不设上限，如果大于等于[1]表示设置了上限。其他数值都是无效数值。
     */
    private var mLimitCount: Int = NO_LIMIT_COUNT

    /**
     * 是否显示所有照片的相册
     */
    private var mAllPhotosAlbum: Boolean = true

    /**
     * 是否记录用户的选择次序
     */
    private var mCountable: Boolean = false

    /**
     * 是否支持预览选中的图片
     */
    private var mPreview: Boolean = false

    /**
     * 多选-无上限模式下，是否允许全选当前文件夹下的图片
     */
    private var mSelectableAll: Boolean = false

    private lateinit var requestPermissionBtn: Button

    companion object {
        private val STORAGE_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        private const val RC_STORAGE_PERMISSIONS = 1
        private const val RC_PHOTO_VIEW = 2
        private const val RC_APP_SETTINGS = 3
        private const val EXTRA_CHECKED_PHOTOS = "extra.CHECKED_PHOTOS"

        fun newInstance(allPhotosAlbum: Boolean, choiceMode: Int, limitCount: Int, countable: Boolean, preview: Boolean, selectableAll: Boolean): PhotoPickerFragment {
            val arguments = Bundle()
            arguments.putBoolean(EXTRA_ALL_PHOTOS_ALBUM, allPhotosAlbum)
            arguments.putInt(EXTRA_CHOICE_MODE, choiceMode)
            arguments.putInt(EXTRA_LIMIT_COUNT, limitCount)
            arguments.putBoolean(EXTRA_COUNTABLE, countable)
            arguments.putBoolean(EXTRA_PREVIEW, preview)
            arguments.putBoolean(EXTRA_SELECTABLE_ALL, selectableAll)

            val fragment = PhotoPickerFragment()
            fragment.arguments = arguments
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnPhotoPickerListener) {
            mOnPhotoPickerListener = context
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAlbumsAdapter = AlbumsAdapter(requireContext(), arrayListOf())

        mCheckedPhotos = if (savedInstanceState != null) {
            savedInstanceState.getStringArrayList(EXTRA_CHECKED_PHOTOS)!!
        } else {
            ArrayList(0)
        }

        // arguments不能为空
        val arguments = requireArguments()

        // 设置选择模式
        val choiceMode = arguments.getInt(EXTRA_CHOICE_MODE)
        check(choiceMode == CHOICE_MODE_SINGLE
                || choiceMode == CHOICE_MODE_MULTIPLE_NO_UPPER_LIMIT
                || choiceMode == CHOICE_MODE_MULTIPLE_UPPER_LIMIT) { "Invalid choice mode: $choiceMode" }
        mChoiceMode = choiceMode

        val limitCount = arguments.getInt(EXTRA_LIMIT_COUNT)
        check(limitCount == NO_LIMIT_COUNT || limitCount >= 1) { "Invalid limit count: $limitCount" }
        mLimitCount = limitCount

        mAllPhotosAlbum = arguments.getBoolean(EXTRA_ALL_PHOTOS_ALBUM, true)

        mCountable = arguments.getBoolean(EXTRA_COUNTABLE, false)

        mPreview = arguments.getBoolean(EXTRA_PREVIEW, true)

        mSelectableAll = arguments.getBoolean(EXTRA_SELECTABLE_ALL, false)

        setHasOptionsMenu(true)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_photo_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findViewById<View>(R.id.tv_photo_picker_selected_album_label)!!.setOnClickListener {
            showAlbumPicker()
        }
        val selectedToggle = findViewById<View>(R.id.tv_photo_picker_selected_toggle)!!
        selectedToggle.visibility = if (mSelectableAll) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
        selectedToggle.setOnClickListener {
            toggleCheckAll()
        }
        val previewView = findViewById<View>(R.id.tv_photo_picker_preview)!!
        previewView.visibility = if (mPreview) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
        previewView.setOnClickListener { showPhotoPreview() }
        requestPermissionBtn = findViewById(R.id.requestPermissionBtn)!!
        requestPermissionBtn.setOnClickListener { showStorageRationale() }

        initThemeConfig()

        onPhotosSelect()
        updateToggleText()
    }

    override fun onResume() {
        super.onResume()
        if (hasStoragePermissions()) {
            loadAlbums()
        } else {
            setStatusIndicator(getString(R.string.module_photo_picker_read_external_storage_denied))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putSerializable(EXTRA_CHECKED_PHOTOS, mCheckedPhotos)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (RC_STORAGE_PERMISSIONS == requestCode) {
            if (!hasStoragePermissions()) {
                showToast(R.string.module_photo_picker_read_external_storage_denied)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RC_PHOTO_VIEW -> {
                if (Activity.RESULT_OK == resultCode) {
                    mCheckedPhotos = data!!.getStringArrayListExtra(PhotoViewActivity.EXTRA_CHECKED_PHOTOS)!!
                    onPhotosSelect()
                    updateToggleText()
                    mPhotosAdapter!!.notifyDataSetChanged()
                }
            }
        }
    }

    private fun setLoadingIndicator(active: Boolean) {
        if (view == null) return
        val indicatorRv = findViewById<View>(R.id.rv_photo_picker_indicator)!!
        val loadingIndicator = findViewById<View>(R.id.loadingIndicator)!!
        val statusIndicator = findViewById<View>(R.id.statusIndicator)!!
        indicatorRv.post {
            if (active) {
                indicatorRv.visibility = View.VISIBLE
                loadingIndicator.visibility = View.VISIBLE
                statusIndicator.visibility = View.INVISIBLE
                requestPermissionBtn.visibility = View.INVISIBLE
            } else {
                indicatorRv.visibility = View.INVISIBLE
                loadingIndicator.visibility = View.INVISIBLE
                statusIndicator.visibility = View.VISIBLE
                requestPermissionBtn.visibility = View.VISIBLE
            }
        }
    }

    private fun setStatusIndicator(statusText: String) {
        if (view == null) return
        val indicatorRv = findViewById<View>(R.id.rv_photo_picker_indicator)!!
        val loadingIndicator = findViewById<View>(R.id.loadingIndicator)!!
        val statusIndicator = findViewById<TextView>(R.id.statusIndicator)!!
        indicatorRv.post {
            indicatorRv.visibility = View.VISIBLE
            loadingIndicator.visibility = View.INVISIBLE
            statusIndicator.visibility = View.VISIBLE
            requestPermissionBtn.visibility = View.VISIBLE
            statusIndicator.text = statusText
        }
    }

    private fun initThemeConfig() {
        val themeConfig = PhotoPicker.themeConfig
        // 底部导航条背景色
        findViewById<View>(R.id.ll_photo_picker_bottom_bar)!!.setBackgroundColor(themeConfig.bottomBarBackgroundColor)
        // 底部导航条文字颜色
        val albumLabelText = findViewById<TextView>(R.id.tv_photo_picker_selected_album_label)!!
        findViewById<TextView>(R.id.tv_photo_picker_preview)!!.setTextColor(themeConfig.bottomBarTextColor)
        findViewById<TextView>(R.id.tv_photo_picker_selected_toggle)!!.setTextColor(themeConfig.bottomBarTextColor)
        albumLabelText.setTextColor(themeConfig.bottomBarTextColor)
        val arrowDrawable = ThemeConfig.tint(requireContext(), R.drawable.ic_arrow_drop_up_black_24dp, PhotoPicker.themeConfig.arrowDropColor)
        albumLabelText.setCompoundDrawablesWithIntrinsicBounds(null, null, arrowDrawable, null)
    }

    private fun loadAlbums() {
        if (mSelectedAlbum == null) {
            AlbumsAsyncTask(this).execute()
        }
    }

    private fun loadPhotos(album: Album) {
        PhotosAsyncTask(this, album).execute()
    }

    private fun showNoAlbums() {
        val indicatorRv = findViewById<View>(R.id.rv_photo_picker_indicator)!!
        val bottomBar = findViewById<View>(R.id.ll_photo_picker_bottom_bar)!!
        indicatorRv.visibility = View.VISIBLE
        bottomBar.visibility = View.INVISIBLE
    }

    private fun showAlbums(albums: List<Album>) {
        mAlbumsAdapter.replaceData(albums)

        if (mSelectedAlbum == null) {
            mAlbumsAdapter.setCheckedPosition(0)
            val album = albums[0]
            setSelectedAlbum(album)
            PhotosAsyncTask(this, album).execute()
        }
    }

    private fun showNoPhotos() {

    }

    private fun showPhotos(photos: List<Photo>) {
        val gridView: GridView? = findViewById(R.id.gridView)
        when (mChoiceMode) {
            CHOICE_MODE_SINGLE -> {
                mPhotosAdapter = UnorderedPhotosAdapter(this, photos)
            }
            CHOICE_MODE_MULTIPLE_UPPER_LIMIT -> {
                mPhotosAdapter = if (mCountable) {
                    OrderedPhotosAdapter(this, photos)
                } else {
                    UnorderedPhotosAdapter(this, photos)
                }
            }
            CHOICE_MODE_MULTIPLE_NO_UPPER_LIMIT -> {
                mPhotosAdapter = if (mCountable) {
                    OrderedPhotosAdapter(this, photos)
                } else {
                    UnorderedPhotosAdapter(this, photos)
                }
            }
        }
        gridView?.adapter = mPhotosAdapter
        updateToggleText()
    }

    private fun setSelectedAlbum(album: Album) {
        mSelectedAlbum = album
        val selectedAlbumTv = findViewById<TextView>(R.id.tv_photo_picker_selected_album_label)
        selectedAlbumTv?.text = album.name
    }

    private fun showAlbumPicker() {
        val gridView = findViewById<GridView>(R.id.gridView) ?: return

        val popupWindow = ListPopupWindow(requireContext())
        popupWindow.width = gridView.width
        popupWindow.height = gridView.height

        popupWindow.setBackgroundDrawable(ColorDrawable(PhotoPicker.themeConfig.albumPickerBackgroundColor))
        popupWindow.anchorView = requireView().findViewById(R.id.popup_anchor)
        popupWindow.isModal = true
        popupWindow.setAdapter(mAlbumsAdapter)
        popupWindow.setOnItemClickListener { _, _, position, _ ->
            val album = mAlbumsAdapter.getItem(position)
            mAlbumsAdapter.setCheckedPosition(position)
            setSelectedAlbum(album)
            loadPhotos(album)
            popupWindow.dismiss()
        }
        popupWindow.show()
    }

    private fun isPhotoChecked(photo: Photo): Boolean {
        return mCheckedPhotos.contains(photo.absolutePath)
    }

    private fun uncheckedPhoto(photo: Photo) {
        if (mCheckedPhotos.contains(photo.absolutePath)) {
            mCheckedPhotos.remove(photo.absolutePath)
        }
    }

    private fun checkPhoto(photo: Photo) {
        if (mCheckedPhotos.contains(photo.absolutePath).not()) {
            mCheckedPhotos.add(photo.absolutePath)
        }
    }

    private fun indexOfPhoto(photo: Photo): Int {
        return mCheckedPhotos.indexOf(photo.absolutePath)
    }

    fun getAllCheckedPhotos(): ArrayList<String> {
        val photos = arrayListOf<String>()
        mCheckedPhotos.forEach { it ->
            photos.add(it)
        }
        return photos
    }

    /**
     * 当前选中的相册下的图片是否全部被选中
     */
    private fun isCheckedAll(): Boolean {
        /*val photos = mCheckedPhotos[mPhotosAdapter!!.getAlbumPath()]
        return (photos == null || photos.isEmpty() || photos.size < mPhotosAdapter!!.getItemCount()).not()*/
        checkNotNull(mPhotosAdapter)
        mPhotosAdapter!!.getValues().forEach {
            if (mCheckedPhotos.contains(it.absolutePath).not()) {
                return false
            }
        }
        return true
    }

    /**
     * 当前相册下的图片全选或者全不选
     */
    private fun toggleCheckAll() {
        if (mPhotosAdapter == null) return
        // 全不选
        if (isCheckedAll()) {
            mPhotosAdapter!!.getValues().forEach {
                uncheckedPhoto(it)
            }
        } else {
            mPhotosAdapter!!.getValues().forEach {
                checkPhoto(it)
            }
        }
        onPhotosSelect()
        updateToggleText()
        mPhotosAdapter!!.notifyDataSetChanged()
    }

    /**
     * 1. 回调 [OnPhotoPickerListener.onPhotosSelect]
     * 2. 是否启用"预览按钮"
     */
    private fun onPhotosSelect() {
        val checkedPhotos = getAllCheckedPhotos()
        mOnPhotoPickerListener?.onPhotosSelect(checkedPhotos)
        val previewTv = findViewById<View>(R.id.tv_photo_picker_preview)
        previewTv?.isEnabled = checkedPhotos.isNotEmpty()
    }

    private fun showPhotoPreview() {
        if (context == null) return
        val intent = PhotoViewActivity.makeIntent(requireContext(), getAllCheckedPhotos())
        startActivityForResult(intent, RC_PHOTO_VIEW)
    }

    /**
     * 更新"全选"或"全不选"按钮上的文字
     */
    private fun updateToggleText() {
        if (view == null || mPhotosAdapter == null) {
            return
        }
        val toggleText = requireView().findViewById<TextView>(R.id.tv_photo_picker_selected_toggle)
        // 只有不设上限的多选模式才支持全选功能
        if (CHOICE_MODE_MULTIPLE_NO_UPPER_LIMIT == mChoiceMode) {
            toggleText.setText(if (isCheckedAll()) {
                R.string.module_photo_picker_unselected_all
            } else {
                R.string.module_photo_picker_select_all
            })
        } else {
            if (toggleText.visibility == View.VISIBLE) {
                toggleText.visibility = View.INVISIBLE
            }
        }
    }

    private fun allowsToCheck(): Boolean {
        return mLimitCount == NO_LIMIT_COUNT || mCheckedPhotos.size < mLimitCount
    }

    private fun hasStoragePermissions(): Boolean {
        for (permission in STORAGE_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(requireActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun showStorageRationale() {
        AlertDialog.Builder(requireActivity())
                .setTitle(R.string.module_photo_picker_read_external_storage_rationale)
                .setPositiveButton(R.string.module_photo_picker_positive) { _, _ ->
                    // 请求权限
                    requestPermissions(STORAGE_PERMISSIONS, RC_STORAGE_PERMISSIONS)
                }
                .setNegativeButton(R.string.module_photo_picker_negative) { _, _ -> }
                .setNeutralButton(R.string.module_photo_picker_neutral) { _, _ ->
                    // 系统设置
                    showAppSettings()
                }
                .show()
    }

    private fun showAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.fromParts("package", requireActivity().packageName, null))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivityForResult(intent, RC_APP_SETTINGS)
        } catch (e: Exception) {
            showToast(R.string.module_photo_picker_error_open_failed_no_apps)
        }
    }

    /**
     * 无序多选照片适配器
     */
    private class UnorderedPhotosAdapter(fragment: PhotoPickerFragment, photos: List<Photo>) : AbsPhotosAdapter(fragment, R.layout.grid_item_photo, photos) {

        val checkboxOutlineDrawable = ThemeConfig.tint(fragment.requireContext(), R.drawable.ic_check_box_outline_blank_black_24dp, PhotoPicker.themeConfig.checkboxOutlineColor)
        val checkboxDrawable = ThemeConfig.tint(fragment.requireContext(), R.drawable.ic_check_box_black_24dp, PhotoPicker.themeConfig.checkboxColor)

        override fun bindView(viewHolder: ViewHolder, value: Photo, position: Int) {
            val thumbIv = viewHolder.findViewById<ImageView>(R.id.iv_photo_picker_photo_thumb)!!
            PhotoPicker.photoLoader.loadPhoto(thumbIv, value.absolutePath, itemSize, itemSize)

            val checkboxIv = viewHolder.findViewById<ImageView>(R.id.iv_photo_picker_photo_checkbox)!!
            if (fragment.isPhotoChecked(value)) {
                checkboxIv.setImageDrawable(checkboxDrawable)
            } else {
                checkboxIv.setImageDrawable(checkboxOutlineDrawable)
            }
            thumbIv.setOnClickListener {
                if (fragment.isPhotoChecked(value)) {
                    fragment.uncheckedPhoto(value)
                    checkboxIv.setImageDrawable(checkboxOutlineDrawable)
                    fragment.updateToggleText()
                    fragment.onPhotosSelect()
                } else {
                    if (CHOICE_MODE_MULTIPLE_NO_UPPER_LIMIT == fragment.mChoiceMode
                            || CHOICE_MODE_MULTIPLE_UPPER_LIMIT == fragment.mChoiceMode) {
                        if (fragment.allowsToCheck()) {
                            fragment.checkPhoto(value)
                            checkboxIv.setImageDrawable(checkboxDrawable)
                            fragment.updateToggleText()
                            fragment.onPhotosSelect()
                        } else {
                            fragment.showToast(fragment.getString(R.string.module_photo_picker_max_limit, fragment.mLimitCount))
                        }
                    } else {
                        fragment.mCheckedPhotos.clear()
                        fragment.checkPhoto(value)
                        notifyDataSetChanged()
                        checkboxIv.setImageDrawable(checkboxDrawable)
                        fragment.updateToggleText()
                        fragment.onPhotosSelect()
                    }
                }
            }
        }
    }

    /**
     * 有序多选照片适配器
     */
    private class OrderedPhotosAdapter(fragment: PhotoPickerFragment, photos: List<Photo>) : AbsPhotosAdapter(fragment, R.layout.grid_item_ordered_photo, photos) {
        @SuppressLint("SetTextI18n")
        override fun bindView(viewHolder: ViewHolder, value: Photo, position: Int) {
            val thumbIv = viewHolder.findViewById<ImageView>(R.id.iv_photo_picker_photo_thumb)!!
            PhotoPicker.photoLoader.loadPhoto(thumbIv, value.absolutePath, itemSize, itemSize)

            val checkedOrderTv = viewHolder.findViewById<TextView>(R.id.iv_photo_picker_photo_checked_order)!!
            val index = fragment.indexOfPhoto(value)
            if (index == -1) {
                // checkedOrderTv.visibility = View.INVISIBLE
                checkedOrderTv.text = ""
                checkedOrderTv.setBackgroundResource(PhotoPicker.themeConfig.orderedUncheckedBackground)
            } else {
                // checkedOrderTv.visibility = View.VISIBLE
                checkedOrderTv.setBackgroundResource(PhotoPicker.themeConfig.orderedCheckedBackground)
                checkedOrderTv.text = "${index + 1}"
            }

            thumbIv.setOnClickListener {
                if (fragment.isPhotoChecked(value)) {
                    fragment.uncheckedPhoto(value)
                    notifyDataSetChanged()
                    fragment.updateToggleText()
                    fragment.onPhotosSelect()
                } else {
                    if (fragment.allowsToCheck()) {
                        fragment.checkPhoto(value)
                        notifyDataSetChanged()
                        fragment.updateToggleText()
                        fragment.onPhotosSelect()
                    } else {
                        fragment.showToast(fragment.getString(R.string.module_photo_picker_max_limit, fragment.mLimitCount))
                    }
                }
            }
        }
    }

    private abstract class AbsPhotosAdapter(protected val fragment: PhotoPickerFragment, resource: Int, photos: List<Photo>) : CommonAdapter<Photo>(resource, photos) {
        protected val itemSize: Int

        init {
            val dm = DisplayMetrics()
            val activity = fragment.requireActivity()
            activity.windowManager.defaultDisplay.getMetrics(dm)
            val numColumns = activity.resources.getInteger(R.integer.grid_num_columns)
            val horizontalSpacing = activity.resources.getDimensionPixelSize(R.dimen.grid_horizontal_spacing)
            itemSize = (dm.widthPixels - (numColumns + 1) * horizontalSpacing) / numColumns
        }
    }

    private class AlbumsAdapter(context: Context, albums: List<Album>) : CommonAdapter<Album>(R.layout.list_item_album, albums) {
        private var mCheckedPosition = -1

        private val itemSize: Int = context.resources.getDimensionPixelSize(R.dimen.album_cover_size)

        override fun bindView(viewHolder: ViewHolder, value: Album, position: Int) {
            val coverIv = viewHolder.findViewById<ImageView>(R.id.iv_photo_picker_album_cover)!!
            PhotoPicker.photoLoader.loadPhoto(coverIv, value.photos.first().absolutePath, itemSize, itemSize)

            val albumName = viewHolder.findViewById<TextView>(R.id.tv_photo_picker_album_name)!!
            albumName.setTextColor(PhotoPicker.themeConfig.albumPickerItemTextColor)
            albumName.text = value.name
            val photoCount = viewHolder.findViewById<TextView>(R.id.tv_photo_picker_photo_count)!!
            photoCount.text = "${value.photos.size}"
            photoCount.setTextColor(PhotoPicker.themeConfig.albumPickerItemTextColor)

            viewHolder.findViewById<ImageView>(R.id.iv_photo_picker_album_checkbox)?.setColorFilter(PhotoPicker.themeConfig.radioCheckedColor)

            if (mCheckedPosition == position) {
                viewHolder.findViewById<ImageView>(R.id.iv_photo_picker_album_checkbox)?.visibility = View.VISIBLE
            } else {
                viewHolder.findViewById<ImageView>(R.id.iv_photo_picker_album_checkbox)?.visibility = View.INVISIBLE
            }
        }

        fun setCheckedPosition(checkedPosition: Int) {
            if (mCheckedPosition != checkedPosition) {
                mCheckedPosition = checkedPosition
                notifyDataSetChanged()
            }
        }
    }

    private class AlbumsAsyncTask(fragment: PhotoPickerFragment) : AsyncTask<Void, Void, List<Album>>() {
        private val mReference = WeakReference(fragment)

        override fun onPreExecute() {
            super.onPreExecute()
            mReference.get()?.setLoadingIndicator(true)
        }

        override fun doInBackground(vararg params: Void?): List<Album> {
            val fragment = mReference.get() ?: return listOf()
            val context = fragment.context ?: return listOf()
            return PhotosRepository(context).listAlbums(fragment.mAllPhotosAlbum)
        }

        override fun onPostExecute(result: List<Album>) {
            if (result.isEmpty()) {
                mReference.get()?.showNoAlbums()
            } else {
                mReference.get()?.showAlbums(result)
            }
            mReference.get()?.setLoadingIndicator(false)
        }
    }

    private class PhotosAsyncTask(fragment: PhotoPickerFragment, private val album: Album) : AsyncTask<Void, Void, List<Photo>>() {
        private val mReference = WeakReference(fragment)

        override fun doInBackground(vararg params: Void): List<Photo> {
            val fragment = mReference.get() ?: return listOf()
            val context = fragment.context ?: return listOf()
            return PhotosRepository(context).listPhotos(album)
        }

        override fun onPostExecute(result: List<Photo>) {
            super.onPostExecute(result)

            if (result.isEmpty()) {
                mReference.get()?.showNoPhotos()
            } else {
                mReference.get()?.showPhotos(result)
            }
        }
    }

    interface OnPhotoPickerListener {
        /**
         * 更新选中的图片时回调
         */
        fun onPhotosSelect(photoPaths: ArrayList<String>)
    }
}