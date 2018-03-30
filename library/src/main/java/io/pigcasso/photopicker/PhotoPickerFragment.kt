package io.pigcasso.photopicker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.ListPopupWindow
import android.util.DisplayMetrics
import android.view.*
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import java.lang.ref.WeakReference


/**
 * @author Zhu Liang
 */
class PhotoPickerFragment : Fragment() {

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

    companion object {
        private const val PERMISSIONS = Manifest.permission.READ_EXTERNAL_STORAGE
        private const val RC_READ_EXTERNAL_STORAGE = 1
        private const val EXTRA_CHECKED_PHOTOS = "extra.CHECKED_PHOTOS"

        fun newInstance(allPhotosAlbum: Boolean, choiceMode: Int, limitCount: Int, countable: Boolean): PhotoPickerFragment {
            val arguments = Bundle()
            arguments.putBoolean(EXTRA_ALL_PHOTOS_ALBUM, allPhotosAlbum)
            arguments.putInt(EXTRA_CHOICE_MODE, choiceMode)
            arguments.putInt(EXTRA_LIMIT_COUNT, limitCount)
            arguments.putBoolean(EXTRA_COUNTABLE, countable)

            val fragment = PhotoPickerFragment()
            fragment.arguments = arguments
            return fragment
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is OnPhotoPickerListener) {
            mOnPhotoPickerListener = context
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAlbumsAdapter = AlbumsAdapter(context!!, arrayListOf())

        mCheckedPhotos = if (savedInstanceState != null) {
            savedInstanceState.getStringArrayList(EXTRA_CHECKED_PHOTOS)
        } else {
            ArrayList(0)
        }

        // arguments不能为空
        checkNotNull(arguments)

        // 设置选择模式
        val choiceMode = arguments!!.getInt(EXTRA_CHOICE_MODE)
        check(choiceMode == CHOICE_MODE_SINGLE
                || choiceMode == CHOICE_MODE_MULTIPLE_NO_UPPER_LIMIT
                || choiceMode == CHOICE_MODE_MULTIPLE_UPPER_LIMIT, { "Invalid choice mode: $choiceMode" })
        mChoiceMode = choiceMode

        val limitCount = arguments!!.getInt(EXTRA_LIMIT_COUNT)
        check(limitCount == NO_LIMIT_COUNT || limitCount >= 1, { "Invalid limit count: $limitCount" })
        mLimitCount = limitCount

        mAllPhotosAlbum = arguments!!.getBoolean(EXTRA_ALL_PHOTOS_ALBUM, true)

        mCountable = arguments!!.getBoolean(EXTRA_COUNTABLE, false)

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
        findViewById<View>(R.id.tv_photo_picker_selected_toggle)!!.setOnClickListener {
            toggleCheckAll()
        }
        findViewById<View>(R.id.tv_photo_picker_preview)!!.setOnClickListener {
            showPhotoPreview()
        }

        initThemeConfig()

        onPhotosSelect()
        updateToggleText()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val menuItem = menu.add(R.id.group_menu_photo_picker_select_done, R.id.menu_photo_picker_select_done,
                Menu.NONE, R.string.module_photo_picker_select_done)
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_photo_picker_select_done -> {
                selectDone()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        if (EasyPermissions.hasPermissions(context!!, PERMISSIONS)) {
            loadAlbums()
        } else {
            EasyPermissions.requestPermissions(PermissionRequest.Builder(this, RC_READ_EXTERNAL_STORAGE, PERMISSIONS).build())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putSerializable(EXTRA_CHECKED_PHOTOS, mCheckedPhotos)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    @AfterPermissionGranted(RC_READ_EXTERNAL_STORAGE)
    private fun requestPermissions() {
        if (EasyPermissions.hasPermissions(context!!, PERMISSIONS)) {
            loadAlbums()
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.module_photo_picker_read_external_storage_rationale),
                    RC_READ_EXTERNAL_STORAGE, PERMISSIONS)
        }
    }

    fun setLoadingIndicator(active: Boolean) {
        if (view == null) return
        val indicatorRv = findViewById<View>(R.id.rv_photo_picker_indicator)!!
        val loadingIndicator = findViewById<View>(R.id.loadingIndicator)!!
        val statusIndicator = findViewById<View>(R.id.statusIndicator)!!
        indicatorRv.post({
            if (active) {
                indicatorRv.visibility = View.VISIBLE
                loadingIndicator.visibility = View.VISIBLE
                statusIndicator.visibility = View.INVISIBLE
            } else {
                indicatorRv.visibility = View.INVISIBLE
                loadingIndicator.visibility = View.INVISIBLE
                statusIndicator.visibility = View.VISIBLE
            }
        })
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
        val arrowDrawable = ThemeConfig.tint(context!!, R.drawable.ic_arrow_drop_up_black_24dp, PhotoPicker.themeConfig.arrowDropColor)
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

    fun showNoAlbums() {
        val indicatorRv = findViewById<View>(R.id.rv_photo_picker_indicator)
        indicatorRv?.post({
            indicatorRv.visibility = View.VISIBLE
        })
    }

    fun showAlbums(albums: List<Album>) {
        mAlbumsAdapter.replaceData(albums)

        if (mSelectedAlbum == null) {
            mAlbumsAdapter.setCheckedPosition(0)
            val album = albums[0]
            setSelectedAlbum(album)
            PhotosAsyncTask(this, album).execute()
        }
    }

    fun showNoPhotos() {

    }

    fun showPhotos(photos: List<Photo>) {
        val gridView = findViewById<GridView>(R.id.gridView)
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
                mPhotosAdapter = UnorderedPhotosAdapter(this, photos)
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

        val popupWindow = ListPopupWindow(context!!)
        popupWindow.width = gridView.width
        popupWindow.height = gridView.height

        popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        popupWindow.anchorView = view!!.findViewById(R.id.popup_anchor)
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

    fun uncheckedPhoto(photo: Photo) {
        if (mCheckedPhotos.contains(photo.absolutePath)) {
            mCheckedPhotos.remove(photo.absolutePath)
        }
    }

    fun checkPhoto(photo: Photo) {
        if (mCheckedPhotos.contains(photo.absolutePath).not()) {
            mCheckedPhotos.add(photo.absolutePath)
        }
    }

    fun indexOfPhoto(photo: Photo): Int {
        return mCheckedPhotos.indexOf(photo.absolutePath)
    }

    /**
     * 用户选好图片后，点击了完成按钮
     */
    private fun selectDone() {
        mOnPhotoPickerListener?.onSelectedResult(getAllCheckedPhotos())
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
        val starter = PhotoViewActivity.makeIntent(context!!, getAllCheckedPhotos())
        startActivity(starter)
    }

    /**
     * 更新"全选"或"全不选"按钮上的文字
     */
    fun updateToggleText() {
        if (view == null || mPhotosAdapter == null) {
            return
        }
        val toggleText = view!!.findViewById<TextView>(R.id.tv_photo_picker_selected_toggle)
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

    /**
     * 无序多选照片适配器
     */
    private class UnorderedPhotosAdapter(fragment: PhotoPickerFragment, photos: List<Photo>) : AbsPhotosAdapter(fragment, R.layout.grid_item_photo, photos) {

        val checkboxOutlineDrawable = ThemeConfig.tint(fragment.context!!, R.drawable.ic_check_box_outline_blank_black_24dp, PhotoPicker.themeConfig.checkboxOutlineColor)
        val checkboxDrawable = ThemeConfig.tint(fragment.context!!, R.drawable.ic_check_box_black_24dp, PhotoPicker.themeConfig.checkboxColor)

        override fun bindView(viewHolder: ViewHolder, value: Photo, position: Int) {
            val thumbIv = viewHolder.findViewById<ImageView>(R.id.iv_photo_picker_photo_thumb)!!
            PhotoPicker.photoLoader.loadPhoto(thumbIv, value.absolutePath, itemSize, itemSize)

            val checkboxIv = viewHolder.findViewById<ImageView>(R.id.iv_photo_picker_photo_checkbox)!!
            if (fragment.isPhotoChecked(value)) {
                checkboxIv.setImageDrawable(checkboxDrawable)
            } else {
                checkboxIv.setImageDrawable(checkboxOutlineDrawable)
            }
            thumbIv.setOnClickListener({
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
            })
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
            val activity = fragment.activity!!
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

            viewHolder.findViewById<TextView>(R.id.tv_photo_picker_album_name)!!.text = value.name
            viewHolder.findViewById<TextView>(R.id.tv_photo_picker_photo_count)!!.text = "${value.photos.size}"

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
        fun onPhotosSelect(photoPaths: List<String>)

        /**
         * 点击完成时回调
         */
        fun onSelectedResult(photoPaths: ArrayList<String>)
    }
}